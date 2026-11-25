package com.rupeek.hotelbooking.domain;

import java.time.Instant;
import java.util.UUID;

import com.rupeek.hotelbooking.common.InvalidStateException;

/** Aggregate for one reservation of one room unit. All state changes go through guarded transitions. */
public final class Booking {
    private final String id = UUID.randomUUID().toString();
    private final String propertyId;
    private final String roomTypeId;
    private final String guestName;
    private final String guestEmail;
    private final DateRange stay;
    private final int guests;
    private final Money totalAmount;
    private final Instant createdAt;
    private final Instant holdExpiresAt;

    private BookingStatus status = BookingStatus.PENDING_PAYMENT;
    private Money refundAmount = Money.ZERO;
    private Instant cancelledAt;

    public Booking(String propertyId, String roomTypeId, String guestName, String guestEmail,
                   DateRange stay, int guests, Money totalAmount, Instant createdAt, Instant holdExpiresAt) {
        this.propertyId = propertyId;
        this.roomTypeId = roomTypeId;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.stay = stay;
        this.guests = guests;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.holdExpiresAt = holdExpiresAt;
    }

    /** True if this booking currently occupies a room unit at {@code now}. */
    public synchronized boolean holdsInventory(Instant now) {
        return status == BookingStatus.CONFIRMED
                || (status == BookingStatus.PENDING_PAYMENT && now.isBefore(holdExpiresAt));
    }

    public synchronized void confirm() {
        transitionTo(BookingStatus.CONFIRMED);
    }

    public synchronized void markPaymentFailed() {
        transitionTo(BookingStatus.PAYMENT_FAILED);
    }

    public synchronized void expire() {
        transitionTo(BookingStatus.EXPIRED);
    }

    public synchronized void cancel(Money refund, Instant now) {
        transitionTo(BookingStatus.CANCELLED);
        this.refundAmount = refund;
        this.cancelledAt = now;
    }

    private void transitionTo(BookingStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateException("Booking " + id + " cannot move from " + status + " to " + target);
        }
        status = target;
    }

    public String getId() { return id; }
    public String getPropertyId() { return propertyId; }
    public String getRoomTypeId() { return roomTypeId; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public DateRange getStay() { return stay; }
    public int getGuests() { return guests; }
    public Money getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getHoldExpiresAt() { return holdExpiresAt; }
    public synchronized BookingStatus getStatus() { return status; }
    public synchronized Money getRefundAmount() { return refundAmount; }
    public synchronized Instant getCancelledAt() { return cancelledAt; }
}
