package com.rupeek.hotelbooking.domain;

import com.rupeek.hotelbooking.common.InvalidRequestException;

public record Location(String city, String locality) {

    public Location {
        if (city == null || city.isBlank()) {
            throw new InvalidRequestException("city is required");
        }
        city = city.trim();
        locality = locality == null ? "" : locality.trim();
    }
}
