package com.rupeek.hotelbooking.payment;

import java.util.Map;

import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.PaymentMethod;

/**
 * Abstraction over a payment method / third-party gateway. To add a method, add a value to
 * {@link PaymentMethod} and one @Component implementing this interface; nothing else changes.
 */
public interface PaymentProcessor {

    PaymentMethod method();

    GatewayResult charge(Money amount, Map<String, String> details, String reference);

    GatewayResult refund(String gatewayReference, Money amount);
}
