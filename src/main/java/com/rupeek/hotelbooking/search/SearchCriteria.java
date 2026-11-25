package com.rupeek.hotelbooking.search;

import java.util.Set;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.domain.Amenity;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Money;

/** City, dates and guests are mandatory; everything else is an optional filter. */
public record SearchCriteria(String city, String locality, DateRange stay, int guests,
                             Money minPricePerNight, Money maxPricePerNight,
                             Integer minStars, Set<Amenity> amenities) {

    public SearchCriteria {
        if (city == null || city.isBlank()) throw new InvalidRequestException("city is required");
        if (stay == null) throw new InvalidRequestException("checkIn and checkOut are required");
        if (guests < 1) throw new InvalidRequestException("guests must be at least 1");
        if (minPricePerNight != null && maxPricePerNight != null && minPricePerNight.compareTo(maxPricePerNight) > 0) {
            throw new InvalidRequestException("minPrice cannot exceed maxPrice");
        }
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
    }
}
