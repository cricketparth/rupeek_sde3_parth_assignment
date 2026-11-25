package com.rupeek.hotelbooking.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.rupeek.hotelbooking.domain.Amenity;
import com.rupeek.hotelbooking.domain.PropertyType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Onboard an owner with 0..n properties. Standalone property = owner with one property. */
public record OwnerRequest(@NotBlank String name, @Valid List<PropertyRequest> properties) {

    public record PropertyRequest(@NotBlank String name, @NotNull PropertyType type, @NotBlank String city,
                                  String locality, @Min(1) @Max(5) int starRating, Set<Amenity> amenities,
                                  @NotEmpty @Valid List<RoomTypeRequest> roomTypes) {
    }

    public record RoomTypeRequest(@NotBlank String name, @Min(1) int maxGuests,
                                  @NotNull @DecimalMin(value = "0.01") BigDecimal pricePerNight,
                                  @Min(1) int totalRooms) {
    }
}
