package com.rupeek.hotelbooking.payment;

import java.time.Clock;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.common.InvalidStateException;
import com.rupeek.hotelbooking.common.NotFoundException;
import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.BookingStatus;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Payment;
import com.rupeek.hotelbooking.domain.PaymentMethod;
import com.rupeek.hotelbooking.domain.PaymentStatus;
import com.rupeek.hotelbooking.inventory.InventoryLockManager;
import com.rupeek.hotelbooking.repository.BookingRepository;
import com.rupeek.hotelbooking.repository.PaymentRepository;

/** Takes payment for a booking; the payment outcome drives the booking state. */
@Service
public class PaymentService {

    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final InventoryLockManager locks;
    private final Clock clock;
    private final Map<PaymentMethod, PaymentProcessor> processors = new EnumMap<>(PaymentMethod.class);

    public PaymentService(BookingRepository bookings, PaymentRepository payments, InventoryLockManager locks,
                          Clock clock, List<PaymentProcessor> processorList) {
        this.bookings = bookings;
        this.payments = payments;
        this.locks = locks;
        this.clock = clock;
        processorList.forEach(p -> processors.put(p.method(), p));
    }

    /**
     * Idempotent per (booking, idempotencyKey): replaying the same key returns the original result
     * and never charges twice. Runs under the room-type lock so an expiring hold cannot be confirmed
     * after its inventory has been handed to someone else.
     */
    public PaymentOutcome pay(String bookingId, PaymentMethod method, Map<String, String> details, String idempotencyKey) {
        if (method == null) throw new InvalidRequestException("payment method is required");
        Booking booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking", bookingId));

        return locks.withLock(booking.getRoomTypeId(), () -> {
            Optional<Payment> replay = idempotencyKey == null
                    ? Optional.empty()
                    : payments.findByBookingIdAndIdempotencyKey(bookingId, idempotencyKey);
            if (replay.isPresent()) {
                return new PaymentOutcome(replay.get(), booking);
            }
            return charge(booking, method, details, idempotencyKey);
        });
    }

    /** Refund the successful payment of a booking (called when a confirmed booking is cancelled). */
    public Optional<Payment> refund(String bookingId, Money amount) {
        Optional<Payment> paid = payments.findByBookingId(bookingId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .findFirst();
        paid.ifPresent(payment -> {
            GatewayResult result = processorFor(payment.getMethod()).refund(payment.getGatewayReference(), amount);
            if (!result.success()) {
                throw new InvalidStateException("Refund failed: " + result.message());
            }
            payment.markRefunded(amount);
            payments.save(payment);
        });
        return paid;
    }

    public List<Payment> paymentsFor(String bookingId) {
        return payments.findByBookingId(bookingId);
    }

    private PaymentOutcome charge(Booking booking, PaymentMethod method, Map<String, String> details, String key) {
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new InvalidStateException("Booking " + booking.getId() + " is " + booking.getStatus() + ", not awaiting payment");
        }
        if (!booking.holdsInventory(clock.instant())) {
            booking.expire();
            bookings.save(booking);
            throw new InvalidStateException("Booking hold expired; please book again");
        }

        GatewayResult result = processorFor(method).charge(booking.getTotalAmount(), details, booking.getId());
        Payment payment = payments.save(new Payment(booking.getId(), method, booking.getTotalAmount(), key,
                result.success(), result.reference(), result.success() ? null : result.message(), clock.instant()));

        if (result.success()) {
            booking.confirm();
        } else {
            booking.markPaymentFailed();
        }
        bookings.save(booking);
        return new PaymentOutcome(payment, booking);
    }

    private PaymentProcessor processorFor(PaymentMethod method) {
        PaymentProcessor processor = processors.get(method);
        if (processor == null) {
            throw new InvalidRequestException("Unsupported payment method: " + method);
        }
        return processor;
    }
}
