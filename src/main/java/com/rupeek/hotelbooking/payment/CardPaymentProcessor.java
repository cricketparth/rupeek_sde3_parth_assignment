package com.rupeek.hotelbooking.payment;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.domain.PaymentMethod;

@Component
public class CardPaymentProcessor extends AbstractMockPaymentProcessor {
    public CardPaymentProcessor() {
        super(PaymentMethod.CARD, List.of("cardNumber"));
    }
}
