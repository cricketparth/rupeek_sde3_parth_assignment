package com.rupeek.hotelbooking.search;

import java.util.List;

import com.rupeek.hotelbooking.domain.Property;

/** A property plus the offers still in the running; a filter excludes it by leaving no offers. */
public record PropertyCandidate(Property property, List<RoomOffer> offers) {

    public PropertyCandidate withOffers(List<RoomOffer> newOffers) {
        return new PropertyCandidate(property, newOffers);
    }

    public PropertyCandidate excluded() {
        return withOffers(List.of());
    }

    public boolean isExcluded() {
        return offers.isEmpty();
    }
}
