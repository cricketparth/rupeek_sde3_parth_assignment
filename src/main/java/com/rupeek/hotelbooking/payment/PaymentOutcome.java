package com.rupeek.hotelbooking.payment;

import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.Payment;

public record PaymentOutcome(Payment payment, Booking booking) {
}
