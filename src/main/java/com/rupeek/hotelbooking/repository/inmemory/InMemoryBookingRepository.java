package com.rupeek.hotelbooking.repository.inmemory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.repository.BookingRepository;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final Map<String, Booking> store = new ConcurrentHashMap<>();

    @Override
    public Booking save(Booking booking) {
        store.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Booking> findByRoomTypeId(String roomTypeId) {
        return store.values().stream().filter(b -> b.getRoomTypeId().equals(roomTypeId)).toList();
    }
}
