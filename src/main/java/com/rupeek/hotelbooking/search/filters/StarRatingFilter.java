package com.rupeek.hotelbooking.search.filters;

import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.search.PropertyCandidate;
import com.rupeek.hotelbooking.search.SearchCriteria;
import com.rupeek.hotelbooking.search.SearchFilter;

@Component
public class StarRatingFilter implements SearchFilter {

    @Override
    public boolean appliesTo(SearchCriteria c) {
        return c.minStars() != null;
    }

    @Override
    public PropertyCandidate apply(SearchCriteria c, PropertyCandidate candidate) {
        return candidate.property().getStarRating() >= c.minStars() ? candidate : candidate.excluded();
    }
}
