package com.rupeek.hotelbooking.repository;

import java.util.List;
import java.util.Optional;

import com.rupeek.hotelbooking.domain.Payment;

public interface PaymentRepository {
    Payment save(Payment payment);
    List<Payment> findByBookingId(String bookingId);
    Optional<Payment> findByBookingIdAndIdempotencyKey(String bookingId, String idempotencyKey);
}
