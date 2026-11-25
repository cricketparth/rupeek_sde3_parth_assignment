package com.rupeek.hotelbooking.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.common.NotFoundException;

/** A single hotel/resort/homestay. Always belongs to an {@link Owner}. */
public final class Property {
    private final String id;
    private final String ownerId;
    private final String name;
    private final PropertyType type;
    private final Location location;
    private final int starRating;
    private final Set<Amenity> amenities;
    private final List<RoomType> roomTypes = new ArrayList<>();

    public Property(String ownerId, String name, PropertyType type, Location location,
                    int starRating, Set<Amenity> amenities) {
        if (name == null || name.isBlank()) throw new InvalidRequestException("property name is required");
        if (type == null) throw new InvalidRequestException("property type is required");
        if (location == null) throw new InvalidRequestException("location is required");
        if (starRating < 1 || starRating > 5) throw new InvalidRequestException("starRating must be between 1 and 5");
        this.id = UUID.randomUUID().toString();
        this.ownerId = ownerId;
        this.name = name.trim();
        this.type = type;
        this.location = location;
        this.starRating = starRating;
        this.amenities = amenities == null || amenities.isEmpty() ? EnumSet.noneOf(Amenity.class) : EnumSet.copyOf(amenities);
    }

    public RoomType addRoomType(String name, int maxGuests, Money pricePerNight, int totalRooms) {
        RoomType roomType = new RoomType(id, name, maxGuests, pricePerNight, totalRooms);
        roomTypes.add(roomType);
        return roomType;
    }

    public RoomType roomType(String roomTypeId) {
        return roomTypes.stream()
                .filter(r -> r.getId().equals(roomTypeId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("RoomType", roomTypeId));
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public PropertyType getType() { return type; }
    public Location getLocation() { return location; }
    public int getStarRating() { return starRating; }
    public Set<Amenity> getAmenities() { return Collections.unmodifiableSet(amenities); }
    public List<RoomType> getRoomTypes() { return Collections.unmodifiableList(roomTypes); }
}
