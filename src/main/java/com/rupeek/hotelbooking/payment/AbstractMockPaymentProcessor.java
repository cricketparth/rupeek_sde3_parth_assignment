package com.rupeek.hotelbooking.payment;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.PaymentMethod;

/**
 * Stand-in for a real gateway. Succeeds unless the details contain {@code simulateFailure=true},
 * which makes failure paths easy to demo and test.
 */
public abstract class AbstractMockPaymentProcessor implements PaymentProcessor {

    public static final String SIMULATE_FAILURE = "simulateFailure";

    private final PaymentMethod method;
    private final List<String> requiredFields;

    protected AbstractMockPaymentProcessor(PaymentMethod method, List<String> requiredFields) {
        this.method = method;
        this.requiredFields = requiredFields;
    }

    @Override
    public PaymentMethod method() {
        return method;
    }

    @Override
    public GatewayResult charge(Money amount, Map<String, String> details, String reference) {
        Map<String, String> safe = details == null ? Map.of() : details;
        for (String field : requiredFields) {
            String value = safe.get(field);
            if (value == null || value.isBlank()) {
                throw new InvalidRequestException(method + " payment requires '" + field + "'");
            }
        }
        if ("true".equalsIgnoreCase(safe.get(SIMULATE_FAILURE))) {
            return GatewayResult.failed(method + " payment declined by gateway");
        }
        return GatewayResult.ok(method + "-" + UUID.randomUUID());
    }

    @Override
    public GatewayResult refund(String gatewayReference, Money amount) {
        return GatewayResult.ok("REFUND-" + gatewayReference);
    }
}
