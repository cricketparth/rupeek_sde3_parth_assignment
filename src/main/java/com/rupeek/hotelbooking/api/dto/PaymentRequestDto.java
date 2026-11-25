package com.rupeek.hotelbooking.api.dto;

import java.util.Map;

import com.rupeek.hotelbooking.domain.PaymentMethod;

import jakarta.validation.constraints.NotNull;

/** details holds method-specific fields, e.g. {"cardNumber": "..."} / {"vpa": "..."} / {"walletId": "..."}. */
public record PaymentRequestDto(@NotNull PaymentMethod method, Map<String, String> details) {
}
