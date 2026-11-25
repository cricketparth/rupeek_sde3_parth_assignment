package com.rupeek.hotelbooking.search.filters;

import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.search.PropertyCandidate;
import com.rupeek.hotelbooking.search.RoomOffer;
import com.rupeek.hotelbooking.search.SearchCriteria;
import com.rupeek.hotelbooking.search.SearchFilter;

/** Keeps offers whose average nightly price falls inside [minPrice, maxPrice]. */
@Component
public class PriceRangeFilter implements SearchFilter {

    @Override
    public boolean appliesTo(SearchCriteria c) {
        return c.minPricePerNight() != null || c.maxPricePerNight() != null;
    }

    @Override
    public PropertyCandidate apply(SearchCriteria c, PropertyCandidate candidate) {
        return candidate.withOffers(candidate.offers().stream().filter(o -> inRange(c, o)).toList());
    }

    private boolean inRange(SearchCriteria c, RoomOffer offer) {
        boolean aboveMin = c.minPricePerNight() == null || offer.averagePricePerNight().compareTo(c.minPricePerNight()) >= 0;
        boolean belowMax = c.maxPricePerNight() == null || offer.averagePricePerNight().compareTo(c.maxPricePerNight()) <= 0;
        return aboveMin && belowMax;
    }
}
