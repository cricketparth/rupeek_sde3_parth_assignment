package com.rupeek.hotelbooking.search.filters;

import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.search.PropertyCandidate;
import com.rupeek.hotelbooking.search.SearchCriteria;
import com.rupeek.hotelbooking.search.SearchFilter;

/** The property must offer ALL requested amenities. */
@Component
public class AmenitiesFilter implements SearchFilter {

    @Override
    public boolean appliesTo(SearchCriteria c) {
        return !c.amenities().isEmpty();
    }

    @Override
    public PropertyCandidate apply(SearchCriteria c, PropertyCandidate candidate) {
        return candidate.property().getAmenities().containsAll(c.amenities()) ? candidate : candidate.excluded();
    }
}
