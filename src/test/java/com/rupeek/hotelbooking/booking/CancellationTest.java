package com.rupeek.hotelbooking.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rupeek.hotelbooking.common.InvalidStateException;
import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.BookingStatus;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.PaymentMethod;
import com.rupeek.hotelbooking.domain.PaymentStatus;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.support.TestFixture;

class CancellationTest {

    private TestFixture f;
    private Property property;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        property = f.propertyWithRooms(1);
    }

    private Booking confirmedBooking(int fromDay, int toDay) {
        Booking b = f.bookingService.book(f.request(property, fromDay, toDay));
        f.paymentService.pay(b.getId(), PaymentMethod.CARD, Map.of("cardNumber", "4111"), null);
        return b;
    }

    @Test
    void fullRefundWhenCancelledAtLeast48HoursBeforeCheckIn() {
        Booking b = confirmedBooking(5, 7); // check-in is ~4 days away
        f.bookingService.cancel(b.getId());

        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        assertEquals(Money.of(2000), b.getRefundAmount());
        assertEquals(PaymentStatus.REFUNDED, f.paymentService.paymentsFor(b.getId()).get(0).getStatus());
    }

    @Test
    void halfRefundBetween24And48Hours() {
        Booking b = confirmedBooking(2, 3);          // check-in 2030-01-03 00:00, now 2030-01-01 10:00 => 38h
        f.bookingService.cancel(b.getId());

        assertEquals(Money.of(500), b.getRefundAmount());
    }

    @Test
    void noRefundWithin24Hours() {
        Booking b = confirmedBooking(1, 2);          // check-in 2030-01-02 00:00 => 14h away
        f.bookingService.cancel(b.getId());

        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        assertEquals(Money.ZERO, b.getRefundAmount());
        assertEquals(PaymentStatus.SUCCESS, f.paymentService.paymentsFor(b.getId()).get(0).getStatus());
    }

    @Test
    void cancellingUnpaidBookingNeedsNoRefund() {
        Booking b = f.bookingService.book(f.request(property, 5, 7));
        f.bookingService.cancel(b.getId());

        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        assertEquals(Money.ZERO, b.getRefundAmount());
    }

    @Test
    void cannotCancelTwice() {
        Booking b = confirmedBooking(5, 7);
        f.bookingService.cancel(b.getId());

        assertThrows(InvalidStateException.class, () -> f.bookingService.cancel(b.getId()));
    }

    @Test
    void cannotCancelConfirmedBookingAfterCheckInStarted() {
        Booking b = confirmedBooking(1, 2);
        f.clock.advance(Duration.ofDays(2));

        assertThrows(InvalidStateException.class, () -> f.bookingService.cancel(b.getId()));
        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
    }

    @Test
    void cannotCancelFailedOrExpiredBookings() {
        Booking failed = f.bookingService.book(f.request(property, 5, 7));
        f.paymentService.pay(failed.getId(), PaymentMethod.UPI, Map.of("vpa", "a@upi", "simulateFailure", "true"), null);

        assertThrows(InvalidStateException.class, () -> f.bookingService.cancel(failed.getId()));
    }
}
