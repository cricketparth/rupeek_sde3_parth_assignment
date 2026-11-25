package com.rupeek.hotelbooking.domain;

/**
 * Booking lifecycle:
 * <pre>
 * PENDING_PAYMENT --pay ok--------> CONFIRMED --cancel--> CANCELLED
 *        |--pay failed-----------> PAYMENT_FAILED
 *        |--hold timed out-------> EXPIRED
 *        |--cancel---------------> CANCELLED
 * </pre>
 * Only PENDING_PAYMENT (while the hold is valid) and CONFIRMED consume inventory.
 */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    PAYMENT_FAILED,
    EXPIRED;

    public boolean canTransitionTo(BookingStatus target) {
        return switch (this) {
            case PENDING_PAYMENT -> target == CONFIRMED || target == PAYMENT_FAILED
                    || target == EXPIRED || target == CANCELLED;
            case CONFIRMED -> target == CANCELLED;
            case CANCELLED, PAYMENT_FAILED, EXPIRED -> false;
        };
    }
}
