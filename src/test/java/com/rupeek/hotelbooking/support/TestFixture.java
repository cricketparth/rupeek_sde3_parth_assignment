package com.rupeek.hotelbooking.support;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.rupeek.hotelbooking.booking.BookingRequest;
import com.rupeek.hotelbooking.booking.BookingService;
import com.rupeek.hotelbooking.booking.TieredCancellationPolicy;
import com.rupeek.hotelbooking.domain.Amenity;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Location;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.domain.PropertyType;
import com.rupeek.hotelbooking.domain.RoomType;
import com.rupeek.hotelbooking.inventory.AvailabilityService;
import com.rupeek.hotelbooking.inventory.InventoryLockManager;
import com.rupeek.hotelbooking.payment.CardPaymentProcessor;
import com.rupeek.hotelbooking.payment.PaymentService;
import com.rupeek.hotelbooking.payment.UpiPaymentProcessor;
import com.rupeek.hotelbooking.payment.WalletPaymentProcessor;
import com.rupeek.hotelbooking.pricing.PerNightPricingStrategy;
import com.rupeek.hotelbooking.property.PropertyDetails;
import com.rupeek.hotelbooking.property.PropertyDetails.RoomTypeDetails;
import com.rupeek.hotelbooking.property.PropertyService;
import com.rupeek.hotelbooking.repository.inmemory.InMemoryBookingRepository;
import com.rupeek.hotelbooking.repository.inmemory.InMemoryOwnerRepository;
import com.rupeek.hotelbooking.repository.inmemory.InMemoryPaymentRepository;
import com.rupeek.hotelbooking.repository.inmemory.InMemoryPropertyRepository;
import com.rupeek.hotelbooking.search.SearchService;
import com.rupeek.hotelbooking.search.filters.AmenitiesFilter;
import com.rupeek.hotelbooking.search.filters.GuestCapacityFilter;
import com.rupeek.hotelbooking.search.filters.LocalityFilter;
import com.rupeek.hotelbooking.search.filters.PriceRangeFilter;
import com.rupeek.hotelbooking.search.filters.StarRatingFilter;

/** Wires the whole object graph by hand (no Spring) so unit tests stay fast. */
public class TestFixture {
    public static final Instant START = Instant.parse("2030-01-01T10:00:00Z");
    public static final LocalDate TODAY = LocalDate.of(2030, 1, 1);

    public final MutableClock clock = new MutableClock(START);
    public final InMemoryPropertyRepository propertyRepo = new InMemoryPropertyRepository();
    public final InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
    public final InMemoryPaymentRepository paymentRepo = new InMemoryPaymentRepository();
    public final PropertyService propertyService = new PropertyService(new InMemoryOwnerRepository(), propertyRepo);
    public final AvailabilityService availability = new AvailabilityService(bookingRepo, clock);
    public final InventoryLockManager locks = new InventoryLockManager();
    public final PaymentService paymentService = new PaymentService(bookingRepo, paymentRepo, locks, clock,
            List.of(new CardPaymentProcessor(), new UpiPaymentProcessor(), new WalletPaymentProcessor()));
    public final BookingService bookingService = new BookingService(propertyRepo, bookingRepo, availability, locks,
            new PerNightPricingStrategy(), new TieredCancellationPolicy(), paymentService, clock, Duration.ofMinutes(15));
    public final SearchService searchService = new SearchService(propertyRepo, availability,
            new PerNightPricingStrategy(), List.of(new GuestCapacityFilter(), new LocalityFilter(),
            new PriceRangeFilter(), new StarRatingFilter(), new AmenitiesFilter()));

    /** One property with a single "Deluxe" room type: 2 guests, 1000/night, {@code rooms} units. */
    public Property propertyWithRooms(int rooms) {
        return property("Taj", "Bengaluru", "Indiranagar", 5, Set.of(Amenity.WIFI, Amenity.POOL), 2, "1000", rooms);
    }

    public Property property(String name, String city, String locality, int stars, Set<Amenity> amenities,
                             int maxGuests, String price, int rooms) {
        var owner = propertyService.registerOwner(name + " Group", List.of());
        return propertyService.addProperty(owner.getId(), new PropertyDetails(name, PropertyType.HOTEL,
                new Location(city, locality), stars, amenities,
                List.of(new RoomTypeDetails("Deluxe", maxGuests, Money.of(price), rooms))));
    }

    public BookingRequest request(Property p, int fromDay, int toDay) {
        return request(p, p.getRoomTypes().get(0), fromDay, toDay, 2);
    }

    public BookingRequest request(Property p, RoomType rt, int fromDay, int toDay, int guests) {
        return new BookingRequest(p.getId(), rt.getId(), "Asha", "asha@example.com",
                new DateRange(TODAY.plusDays(fromDay), TODAY.plusDays(toDay)), guests);
    }
}
