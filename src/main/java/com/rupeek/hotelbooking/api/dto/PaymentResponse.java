package com.rupeek.hotelbooking.api.dto;

import java.math.BigDecimal;

import com.rupeek.hotelbooking.domain.PaymentMethod;
import com.rupeek.hotelbooking.domain.PaymentStatus;
import com.rupeek.hotelbooking.payment.PaymentOutcome;

public record PaymentResponse(String paymentId, PaymentMethod method, PaymentStatus status, BigDecimal amount,
                              String failureReason, BookingResponse booking) {

    public static PaymentResponse from(PaymentOutcome o) {
        return new PaymentResponse(o.payment().getId(), o.payment().getMethod(), o.payment().getStatus(),
                o.payment().getAmount().amount(), o.payment().getFailureReason(), BookingResponse.from(o.booking()));
    }
}
