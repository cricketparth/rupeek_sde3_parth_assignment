package com.rupeek.hotelbooking.domain;

import static com.rupeek.hotelbooking.domain.BookingStatus.CANCELLED;
import static com.rupeek.hotelbooking.domain.BookingStatus.CONFIRMED;
import static com.rupeek.hotelbooking.domain.BookingStatus.EXPIRED;
import static com.rupeek.hotelbooking.domain.BookingStatus.PAYMENT_FAILED;
import static com.rupeek.hotelbooking.domain.BookingStatus.PENDING_PAYMENT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.rupeek.hotelbooking.common.InvalidStateException;

class BookingStatusTest {

    @Test
    void allowedTransitions() {
        assertTrue(PENDING_PAYMENT.canTransitionTo(CONFIRMED));
        assertTrue(PENDING_PAYMENT.canTransitionTo(PAYMENT_FAILED));
        assertTrue(PENDING_PAYMENT.canTransitionTo(EXPIRED));
        assertTrue(PENDING_PAYMENT.canTransitionTo(CANCELLED));
        assertTrue(CONFIRMED.canTransitionTo(CANCELLED));
    }

    @Test
    void terminalStatesAndBackwardsMovesAreRejected() {
        for (BookingStatus terminal : new BookingStatus[] {CANCELLED, PAYMENT_FAILED, EXPIRED}) {
            for (BookingStatus target : BookingStatus.values()) {
                assertFalse(terminal.canTransitionTo(target), terminal + " -> " + target);
            }
        }
        assertFalse(CONFIRMED.canTransitionTo(PENDING_PAYMENT));
        assertFalse(CONFIRMED.canTransitionTo(EXPIRED));
    }

    @Test
    void bookingEnforcesTransitions() {
        Instant now = Instant.parse("2030-01-01T00:00:00Z");
        Booking booking = new Booking("p", "r", "g", null, null, 1, Money.of(1), now, now.plusSeconds(60));
        booking.confirm();
        assertThrows(InvalidStateException.class, booking::expire);
        assertThrows(InvalidStateException.class, booking::markPaymentFailed);
    }
}
