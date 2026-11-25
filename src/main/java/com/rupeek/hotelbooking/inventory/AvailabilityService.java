package com.rupeek.hotelbooking.inventory;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.RoomType;
import com.rupeek.hotelbooking.repository.BookingRepository;

/**
 * Availability is derived, not stored: total rooms minus bookings that currently hold inventory
 * and overlap the dates. Cancelling/expiring a booking therefore releases inventory automatically.
 */
@Service
public class AvailabilityService {

    private final BookingRepository bookings;
    private final Clock clock;

    public AvailabilityService(BookingRepository bookings, Clock clock) {
        this.bookings = bookings;
        this.clock = clock;
    }

    public int availableUnits(RoomType roomType, DateRange stay) {
        Instant now = clock.instant();
        long occupied = bookings.findByRoomTypeId(roomType.getId()).stream()
                .filter(b -> b.holdsInventory(now))
                .filter(b -> b.getStay().overlaps(stay))
                .count();
        return Math.max(0, roomType.getTotalRooms() - (int) occupied);
    }
}
