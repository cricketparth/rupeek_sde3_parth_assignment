package com.rupeek.hotelbooking.search.filters;

import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.search.PropertyCandidate;
import com.rupeek.hotelbooking.search.SearchCriteria;
import com.rupeek.hotelbooking.search.SearchFilter;

@Component
public class LocalityFilter implements SearchFilter {

    @Override
    public boolean appliesTo(SearchCriteria c) {
        return c.locality() != null && !c.locality().isBlank();
    }

    @Override
    public PropertyCandidate apply(SearchCriteria c, PropertyCandidate candidate) {
        boolean match = candidate.property().getLocation().locality().equalsIgnoreCase(c.locality().trim());
        return match ? candidate : candidate.excluded();
    }
}
