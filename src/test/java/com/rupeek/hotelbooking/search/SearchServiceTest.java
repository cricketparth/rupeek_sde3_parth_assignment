package com.rupeek.hotelbooking.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.domain.Amenity;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.support.TestFixture;

class SearchServiceTest {

    private TestFixture f;
    private Property budget;
    private Property luxury;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        budget = f.property("Budget Inn", "Bengaluru", "Majestic", 2, Set.of(Amenity.WIFI), 2, "800", 2);
        luxury = f.property("Grand Palace", "Bengaluru", "Indiranagar", 5, Set.of(Amenity.WIFI, Amenity.POOL), 4, "6000", 1);
        f.property("Goa Beach", "Goa", "Calangute", 4, Set.of(Amenity.POOL), 2, "3000", 1);
    }

    private SearchCriteria criteria(String city, String locality, int guests, Money min, Money max,
                                    Integer stars, Set<Amenity> amenities) {
        return new SearchCriteria(city, locality, new DateRange(TestFixture.TODAY.plusDays(5), TestFixture.TODAY.plusDays(7)),
                guests, min, max, stars, amenities);
    }

    private List<String> names(List<PropertyCandidate> results) {
        return results.stream().map(c -> c.property().getName()).toList();
    }

    @Test
    void findsPropertiesInCityOrderedByCheapestOffer() {
        var results = f.searchService.search(criteria("bengaluru", null, 2, null, null, null, Set.of()));
        assertEquals(List.of("Budget Inn", "Grand Palace"), names(results));
    }

    @Test
    void filtersByLocality() {
        var results = f.searchService.search(criteria("Bengaluru", "Indiranagar", 2, null, null, null, Set.of()));
        assertEquals(List.of("Grand Palace"), names(results));
    }

    @Test
    void filtersByGuestCapacity() {
        var results = f.searchService.search(criteria("Bengaluru", null, 4, null, null, null, Set.of()));
        assertEquals(List.of("Grand Palace"), names(results));
    }

    @Test
    void filtersByPriceRange() {
        var results = f.searchService.search(criteria("Bengaluru", null, 1, Money.of(500), Money.of(1000), null, Set.of()));
        assertEquals(List.of("Budget Inn"), names(results));
    }

    @Test
    void filtersByStarRating() {
        var results = f.searchService.search(criteria("Bengaluru", null, 1, null, null, 4, Set.of()));
        assertEquals(List.of("Grand Palace"), names(results));
    }

    @Test
    void amenitiesMustAllBePresent() {
        assertEquals(List.of("Budget Inn", "Grand Palace"),
                names(f.searchService.search(criteria("Bengaluru", null, 1, null, null, null, Set.of(Amenity.WIFI)))));
        assertEquals(List.of("Grand Palace"),
                names(f.searchService.search(criteria("Bengaluru", null, 1, null, null, null, Set.of(Amenity.WIFI, Amenity.POOL)))));
    }

    @Test
    void excludesPropertiesWithoutAvailabilityForTheDates() {
        f.bookingService.book(f.request(luxury, 5, 7));

        var results = f.searchService.search(criteria("Bengaluru", null, 1, null, null, null, Set.of()));
        assertEquals(List.of("Budget Inn"), names(results));
    }

    @Test
    void offerShowsRemainingUnitsAndStayPrice() {
        f.bookingService.book(f.request(budget, 5, 7));

        var offer = f.searchService.search(criteria("Bengaluru", "Majestic", 1, null, null, null, Set.of()))
                .get(0).offers().get(0);
        assertEquals(1, offer.availableUnits());
        assertEquals(Money.of(1600), offer.totalPrice());
        assertEquals(Money.of(800), offer.averagePricePerNight());
    }

    @Test
    void requiresCityAndValidGuestsAndPriceBounds() {
        assertThrows(InvalidRequestException.class, () -> criteria(" ", null, 1, null, null, null, Set.of()));
        assertThrows(InvalidRequestException.class, () -> criteria("Goa", null, 0, null, null, null, Set.of()));
        assertThrows(InvalidRequestException.class, () -> criteria("Goa", null, 1, Money.of(10), Money.of(5), null, Set.of()));
    }

    @Test
    void unknownCityReturnsEmpty() {
        assertTrue(f.searchService.search(criteria("Atlantis", null, 1, null, null, null, Set.of())).isEmpty());
    }
}
