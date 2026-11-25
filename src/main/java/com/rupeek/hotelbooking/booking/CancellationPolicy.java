package com.rupeek.hotelbooking.booking;

import java.time.Instant;

import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.Money;

/** Pluggable refund rule. Swap the bean to change policy (flexible, strict, non-refundable...). */
public interface CancellationPolicy {
    Money refundFor(Booking booking, Instant checkInInstant, Instant now);
}
