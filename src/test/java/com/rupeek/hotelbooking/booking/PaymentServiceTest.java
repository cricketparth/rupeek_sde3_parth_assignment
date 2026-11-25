package com.rupeek.hotelbooking.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.common.InvalidStateException;
import com.rupeek.hotelbooking.common.NoAvailabilityException;
import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.BookingStatus;
import com.rupeek.hotelbooking.domain.PaymentMethod;
import com.rupeek.hotelbooking.domain.PaymentStatus;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.payment.PaymentOutcome;
import com.rupeek.hotelbooking.support.TestFixture;

class PaymentServiceTest {

    private TestFixture f;
    private Property property;
    private Booking booking;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        property = f.propertyWithRooms(1);
        booking = f.bookingService.book(f.request(property, 5, 7));
    }

    @Test
    void successfulPaymentConfirmsBooking() {
        PaymentOutcome out = f.paymentService.pay(booking.getId(), PaymentMethod.CARD, Map.of("cardNumber", "4111"), null);

        assertEquals(PaymentStatus.SUCCESS, out.payment().getStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void everyPaymentMethodIsSupportedThroughTheSameAbstraction() {
        Map<PaymentMethod, Map<String, String>> details = Map.of(
                PaymentMethod.CARD, Map.of("cardNumber", "4111"),
                PaymentMethod.UPI, Map.of("vpa", "a@upi"),
                PaymentMethod.WALLET, Map.of("walletId", "w-1"));
        details.forEach((method, d) -> {
            Property p = f.propertyWithRooms(1);
            Booking b = f.bookingService.book(f.request(p, 5, 7));
            assertEquals(PaymentStatus.SUCCESS, f.paymentService.pay(b.getId(), method, d, null).payment().getStatus());
        });
    }

    @Test
    void failedPaymentMarksBookingFailedAndReleasesInventory() {
        PaymentOutcome out = f.paymentService.pay(booking.getId(), PaymentMethod.WALLET,
                Map.of("walletId", "w-1", "simulateFailure", "true"), null);

        assertEquals(PaymentStatus.FAILED, out.payment().getStatus());
        assertEquals(BookingStatus.PAYMENT_FAILED, booking.getStatus());
        f.bookingService.book(f.request(property, 5, 7)); // room is free again
    }

    @Test
    void missingMethodSpecificDetailIsRejectedWithoutChangingBooking() {
        assertThrows(InvalidRequestException.class,
                () -> f.paymentService.pay(booking.getId(), PaymentMethod.UPI, Map.of(), null));
        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getStatus());
    }

    @Test
    void sameIdempotencyKeyDoesNotChargeTwice() {
        PaymentOutcome first = f.paymentService.pay(booking.getId(), PaymentMethod.CARD, Map.of("cardNumber", "4111"), "key-1");
        PaymentOutcome replay = f.paymentService.pay(booking.getId(), PaymentMethod.CARD, Map.of("cardNumber", "4111"), "key-1");

        assertSame(first.payment(), replay.payment());
        assertEquals(1, f.paymentService.paymentsFor(booking.getId()).size());
    }

    @Test
    void payingAnAlreadyConfirmedBookingWithNewKeyIsRejected() {
        f.paymentService.pay(booking.getId(), PaymentMethod.CARD, Map.of("cardNumber", "4111"), "key-1");

        assertThrows(InvalidStateException.class,
                () -> f.paymentService.pay(booking.getId(), PaymentMethod.CARD, Map.of("cardNumber", "4111"), "key-2"));
        assertEquals(1, f.paymentService.paymentsFor(booking.getId()).size());
    }

    @Test
    void cannotPayAfterHoldExpired() {
        f.clock.advance(Duration.ofMinutes(20));

        assertThrows(InvalidStateException.class,
                () -> f.paymentService.pay(booking.getId(), PaymentMethod.CARD, Map.of("cardNumber", "4111"), null));
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }

    @Test
    void expiredHoldCannotStealInventoryFromNewBooker() {
        f.clock.advance(Duration.ofMinutes(20));
        Booking second = f.bookingService.book(f.request(property, 5, 7)); // allowed: first hold lapsed

        assertThrows(InvalidStateException.class,
                () -> f.paymentService.pay(booking.getId(), PaymentMethod.CARD, Map.of("cardNumber", "4111"), null));
        assertThrows(NoAvailabilityException.class, () -> f.bookingService.book(f.request(property, 5, 7)));
        assertEquals(BookingStatus.PENDING_PAYMENT, second.getStatus());
    }
}
