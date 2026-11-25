package com.rupeek.hotelbooking.property;

import java.util.List;
import java.util.Set;

import com.rupeek.hotelbooking.domain.Amenity;
import com.rupeek.hotelbooking.domain.Location;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.PropertyType;

/** Command objects for onboarding, kept free of web/DTO concerns. */
public record PropertyDetails(String name, PropertyType type, Location location, int starRating,
                              Set<Amenity> amenities, List<RoomTypeDetails> roomTypes) {

    public record RoomTypeDetails(String name, int maxGuests, Money pricePerNight, int totalRooms) {
    }
}
