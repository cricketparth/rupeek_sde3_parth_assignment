package com.rupeek.hotelbooking.payment;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.domain.PaymentMethod;

@Component
public class WalletPaymentProcessor extends AbstractMockPaymentProcessor {
    public WalletPaymentProcessor() {
        super(PaymentMethod.WALLET, List.of("walletId"));
    }
}
