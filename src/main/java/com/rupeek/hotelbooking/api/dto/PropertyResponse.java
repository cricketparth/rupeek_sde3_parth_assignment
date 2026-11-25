package com.rupeek.hotelbooking.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.rupeek.hotelbooking.domain.Amenity;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.domain.PropertyType;
import com.rupeek.hotelbooking.domain.RoomType;

public record PropertyResponse(String id, String ownerId, String name, PropertyType type, String city,
                               String locality, int starRating, Set<Amenity> amenities,
                               List<RoomTypeResponse> roomTypes) {

    public record RoomTypeResponse(String id, String name, int maxGuests, BigDecimal pricePerNight, int totalRooms) {
        static RoomTypeResponse from(RoomType r) {
            return new RoomTypeResponse(r.getId(), r.getName(), r.getMaxGuests(),
                    r.getBasePricePerNight().amount(), r.getTotalRooms());
        }
    }

    public static PropertyResponse from(Property p) {
        return new PropertyResponse(p.getId(), p.getOwnerId(), p.getName(), p.getType(),
                p.getLocation().city(), p.getLocation().locality(), p.getStarRating(), p.getAmenities(),
                p.getRoomTypes().stream().map(RoomTypeResponse::from).toList());
    }
}
