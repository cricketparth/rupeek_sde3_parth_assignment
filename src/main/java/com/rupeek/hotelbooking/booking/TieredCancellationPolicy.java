package com.rupeek.hotelbooking.booking;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.Money;

/** 100% refund if cancelled 48h+ before check-in, 50% if 24h+, otherwise nothing. */
@Component
public class TieredCancellationPolicy implements CancellationPolicy {

    private static final Duration FULL_REFUND_WINDOW = Duration.ofHours(48);
    private static final Duration PARTIAL_REFUND_WINDOW = Duration.ofHours(24);

    @Override
    public Money refundFor(Booking booking, Instant checkInInstant, Instant now) {
        Duration untilCheckIn = Duration.between(now, checkInInstant);
        if (untilCheckIn.compareTo(FULL_REFUND_WINDOW) >= 0) {
            return booking.getTotalAmount();
        }
        if (untilCheckIn.compareTo(PARTIAL_REFUND_WINDOW) >= 0) {
            return booking.getTotalAmount().percent(50);
        }
        return Money.ZERO;
    }
}
