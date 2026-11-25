package com.rupeek.hotelbooking.search;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.domain.RoomType;
import com.rupeek.hotelbooking.inventory.AvailabilityService;
import com.rupeek.hotelbooking.pricing.PricingStrategy;
import com.rupeek.hotelbooking.repository.PropertyRepository;

/**
 * Pipeline: properties in city -> offers for room types that are free on the dates -> every
 * applicable {@link SearchFilter}. Results are ordered by cheapest offer.
 */
@Service
public class SearchService {

    private final PropertyRepository properties;
    private final AvailabilityService availability;
    private final PricingStrategy pricing;
    private final List<SearchFilter> filters;

    public SearchService(PropertyRepository properties, AvailabilityService availability,
                         PricingStrategy pricing, List<SearchFilter> filters) {
        this.properties = properties;
        this.availability = availability;
        this.pricing = pricing;
        this.filters = filters;
    }

    public List<PropertyCandidate> search(SearchCriteria criteria) {
        List<SearchFilter> active = filters.stream().filter(f -> f.appliesTo(criteria)).toList();

        return properties.findByCity(criteria.city()).stream()
                .map(p -> new PropertyCandidate(p, availableOffers(p, criteria)))
                .map(candidate -> applyFilters(active, criteria, candidate))
                .filter(c -> !c.isExcluded())
                .sorted(Comparator.comparing(SearchService::cheapestOffer))
                .toList();
    }

    private PropertyCandidate applyFilters(List<SearchFilter> active, SearchCriteria criteria, PropertyCandidate candidate) {
        PropertyCandidate current = candidate;
        for (SearchFilter filter : active) {
            if (current.isExcluded()) {
                break;
            }
            current = filter.apply(criteria, current);
        }
        return current;
    }

    private List<RoomOffer> availableOffers(Property property, SearchCriteria criteria) {
        return property.getRoomTypes().stream()
                .map(rt -> toOffer(rt, criteria))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<RoomOffer> toOffer(RoomType roomType, SearchCriteria criteria) {
        int units = availability.availableUnits(roomType, criteria.stay());
        if (units <= 0) {
            return Optional.empty();
        }
        var total = pricing.priceFor(roomType, criteria.stay());
        return Optional.of(new RoomOffer(roomType, units, total, total.dividedBy(criteria.stay().nights())));
    }

    private static java.math.BigDecimal cheapestOffer(PropertyCandidate c) {
        return c.offers().stream().map(o -> o.totalPrice().amount()).min(Comparator.naturalOrder()).orElseThrow();
    }
}
