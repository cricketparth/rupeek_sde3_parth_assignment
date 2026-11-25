package com.rupeek.hotelbooking.payment;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.domain.PaymentMethod;

@Component
public class UpiPaymentProcessor extends AbstractMockPaymentProcessor {
    public UpiPaymentProcessor() {
        super(PaymentMethod.UPI, List.of("vpa"));
    }
}
