package com.rupeek.hotelbooking.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.common.NotFoundException;
import com.rupeek.hotelbooking.domain.Amenity;
import com.rupeek.hotelbooking.domain.Location;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Owner;
import com.rupeek.hotelbooking.domain.PropertyType;
import com.rupeek.hotelbooking.property.PropertyDetails;
import com.rupeek.hotelbooking.property.PropertyDetails.RoomTypeDetails;
import com.rupeek.hotelbooking.support.TestFixture;

class PropertyOnboardingTest {

    private final TestFixture f = new TestFixture();

    private PropertyDetails details(String name, String city) {
        return new PropertyDetails(name, PropertyType.HOTEL, new Location(city, "Centre"), 3,
                Set.of(Amenity.WIFI), List.of(new RoomTypeDetails("Standard", 2, Money.of(1500), 10)));
    }

    @Test
    void standaloneAndChainShareTheSameStructure() {
        Owner standalone = f.propertyService.registerOwner("Solo Stay", List.of(details("Solo", "Pune")));
        Owner chain = f.propertyService.registerOwner("Chain Co",
                List.of(details("Chain Pune", "Pune"), details("Chain Goa", "Goa"), details("Chain Delhi", "Delhi")));

        assertEquals(1, f.propertyService.propertiesOf(standalone.getId()).size());
        assertEquals(3, f.propertyService.propertiesOf(chain.getId()).size());
    }

    @Test
    void propertiesCanBeAddedToExistingOwnerLater() {
        Owner owner = f.propertyService.registerOwner("Growing Co", List.of(details("First", "Pune")));
        f.propertyService.addProperty(owner.getId(), details("Second", "Goa"));

        assertEquals(2, f.propertyService.propertiesOf(owner.getId()).size());
        assertEquals(2, owner.getPropertyIds().size());
    }

    @Test
    void rejectsInvalidOnboarding() {
        Owner owner = f.propertyService.registerOwner("Co", List.of());

        assertThrows(NotFoundException.class, () -> f.propertyService.addProperty("missing", details("X", "Pune")));
        assertThrows(InvalidRequestException.class, () -> f.propertyService.addProperty(owner.getId(),
                new PropertyDetails("NoRooms", PropertyType.HOTEL, new Location("Pune", ""), 3, Set.of(), List.of())));
        assertThrows(InvalidRequestException.class, () -> f.propertyService.addProperty(owner.getId(),
                new PropertyDetails("BadStars", PropertyType.HOTEL, new Location("Pune", ""), 9, Set.of(),
                        List.of(new RoomTypeDetails("S", 2, Money.of(1), 1)))));
        assertThrows(InvalidRequestException.class, () -> f.propertyService.registerOwner(" ", List.of()));
    }
}
