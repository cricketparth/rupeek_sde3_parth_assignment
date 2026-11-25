package com.rupeek.hotelbooking.domain;

import java.util.Objects;
import java.util.UUID;

import com.rupeek.hotelbooking.common.InvalidRequestException;

/** A bookable category of room in a property, with a fixed number of identical units (inventory). */
public final class RoomType {
    private final String id;
    private final String propertyId;
    private final String name;
    private final int maxGuests;
    private final Money basePricePerNight;
    private final int totalRooms;

    public RoomType(String propertyId, String name, int maxGuests, Money basePricePerNight, int totalRooms) {
        if (name == null || name.isBlank()) throw new InvalidRequestException("room type name is required");
        if (maxGuests < 1) throw new InvalidRequestException("maxGuests must be at least 1");
        if (totalRooms < 1) throw new InvalidRequestException("totalRooms must be at least 1");
        if (basePricePerNight == null || !basePricePerNight.isPositive()) {
            throw new InvalidRequestException("price per night must be positive");
        }
        this.id = UUID.randomUUID().toString();
        this.propertyId = Objects.requireNonNull(propertyId);
        this.name = name.trim();
        this.maxGuests = maxGuests;
        this.basePricePerNight = basePricePerNight;
        this.totalRooms = totalRooms;
    }

    public String getId() { return id; }
    public String getPropertyId() { return propertyId; }
    public String getName() { return name; }
    public int getMaxGuests() { return maxGuests; }
    public Money getBasePricePerNight() { return basePricePerNight; }
    public int getTotalRooms() { return totalRooms; }
}
