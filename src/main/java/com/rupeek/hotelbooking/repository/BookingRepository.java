package com.rupeek.hotelbooking.repository;

import java.util.List;
import java.util.Optional;

import com.rupeek.hotelbooking.domain.Booking;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(String id);
    List<Booking> findByRoomTypeId(String roomTypeId);
}
