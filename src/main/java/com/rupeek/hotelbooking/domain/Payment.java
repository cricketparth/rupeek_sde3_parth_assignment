package com.rupeek.hotelbooking.domain;

import java.time.Instant;
import java.util.UUID;

public final class Payment {
    private final String id = UUID.randomUUID().toString();
    private final String bookingId;
    private final PaymentMethod method;
    private final Money amount;
    private final String idempotencyKey;
    private final String gatewayReference;
    private final String failureReason;
    private final Instant createdAt;

    private PaymentStatus status;
    private Money refundedAmount = Money.ZERO;

    public Payment(String bookingId, PaymentMethod method, Money amount, String idempotencyKey,
                   boolean success, String gatewayReference, String failureReason, Instant createdAt) {
        this.bookingId = bookingId;
        this.method = method;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.status = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        this.gatewayReference = gatewayReference;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    public synchronized void markRefunded(Money refunded) {
        this.refundedAmount = refunded;
        this.status = PaymentStatus.REFUNDED;
    }

    public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public PaymentMethod getMethod() { return method; }
    public Money getAmount() { return amount; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getGatewayReference() { return gatewayReference; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public synchronized PaymentStatus getStatus() { return status; }
    public synchronized Money getRefundedAmount() { return refundedAmount; }
}
