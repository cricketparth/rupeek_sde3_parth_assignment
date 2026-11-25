package com.rupeek.hotelbooking.repository.inmemory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.rupeek.hotelbooking.domain.Payment;
import com.rupeek.hotelbooking.repository.PaymentRepository;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {
    private final Map<String, Payment> store = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        store.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public List<Payment> findByBookingId(String bookingId) {
        return store.values().stream().filter(p -> p.getBookingId().equals(bookingId)).toList();
    }

    @Override
    public Optional<Payment> findByBookingIdAndIdempotencyKey(String bookingId, String idempotencyKey) {
        return store.values().stream()
                .filter(p -> p.getBookingId().equals(bookingId) && Objects.equals(p.getIdempotencyKey(), idempotencyKey))
                .findFirst();
    }
}
