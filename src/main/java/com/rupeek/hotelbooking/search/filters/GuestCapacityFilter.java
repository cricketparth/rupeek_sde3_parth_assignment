package com.rupeek.hotelbooking.search.filters;

import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.search.PropertyCandidate;
import com.rupeek.hotelbooking.search.SearchCriteria;
import com.rupeek.hotelbooking.search.SearchFilter;

/** Keeps only room types that can accommodate the requested number of guests. */
@Component
public class GuestCapacityFilter implements SearchFilter {

    @Override
    public boolean appliesTo(SearchCriteria c) {
        return true;
    }

    @Override
    public PropertyCandidate apply(SearchCriteria c, PropertyCandidate candidate) {
        return candidate.withOffers(candidate.offers().stream()
                .filter(o -> o.roomType().getMaxGuests() >= c.guests())
                .toList());
    }
}
