package com.rupeek.hotelbooking.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.common.NoAvailabilityException;
import com.rupeek.hotelbooking.common.NotFoundException;
import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.BookingStatus;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.support.TestFixture;

class BookingServiceTest {

    private TestFixture f;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
    }

    @Test
    void createsPendingBookingWithPriceForAllNights() {
        Property p = f.propertyWithRooms(1);
        Booking b = f.bookingService.book(f.request(p, 5, 8));

        assertEquals(BookingStatus.PENDING_PAYMENT, b.getStatus());
        assertEquals(Money.of(3000), b.getTotalAmount()); // 3 nights x 1000
    }

    @Test
    void overlappingBookingOnLastRoomIsRejected() {
        Property p = f.propertyWithRooms(1);
        f.bookingService.book(f.request(p, 5, 8));

        assertThrows(NoAvailabilityException.class, () -> f.bookingService.book(f.request(p, 7, 10)));
        assertThrows(NoAvailabilityException.class, () -> f.bookingService.book(f.request(p, 5, 8)));
        assertThrows(NoAvailabilityException.class, () -> f.bookingService.book(f.request(p, 6, 7)));
    }

    @Test
    void backToBackStaysAreAllowed() {
        Property p = f.propertyWithRooms(1);
        f.bookingService.book(f.request(p, 5, 8));

        f.bookingService.book(f.request(p, 8, 10)); // checks in the day the other checks out
        f.bookingService.book(f.request(p, 3, 5));  // checks out the day the other checks in
    }

    @Test
    void multipleUnitsAllowParallelStaysUntilExhausted() {
        Property p = f.propertyWithRooms(2);
        f.bookingService.book(f.request(p, 5, 8));
        f.bookingService.book(f.request(p, 5, 8));

        assertThrows(NoAvailabilityException.class, () -> f.bookingService.book(f.request(p, 5, 8)));
    }

    @Test
    void cancellingReleasesInventory() {
        Property p = f.propertyWithRooms(1);
        Booking first = f.bookingService.book(f.request(p, 5, 8));
        f.bookingService.cancel(first.getId());

        f.bookingService.book(f.request(p, 5, 8)); // bookable again
    }

    @Test
    void unpaidHoldExpiresAndReleasesInventory() {
        Property p = f.propertyWithRooms(1);
        f.bookingService.book(f.request(p, 5, 8));
        assertThrows(NoAvailabilityException.class, () -> f.bookingService.book(f.request(p, 5, 8)));

        f.clock.advance(Duration.ofMinutes(16));

        f.bookingService.book(f.request(p, 5, 8));
    }

    @Test
    void rejectsInvalidInput() {
        Property p = f.propertyWithRooms(1);
        var rt = p.getRoomTypes().get(0);

        assertThrows(InvalidRequestException.class, () -> f.bookingService.book(f.request(p, rt, 5, 8, 3))); // > capacity
        assertThrows(InvalidRequestException.class, () -> f.bookingService.book(f.request(p, rt, 5, 8, 0)));
        assertThrows(InvalidRequestException.class, () -> f.bookingService.book(f.request(p, rt, -3, 2, 1))); // past
        assertThrows(NotFoundException.class, () -> f.bookingService.book(
                new BookingRequest("nope", rt.getId(), "A", null, f.request(p, 1, 2).stay(), 1)));
        assertThrows(NotFoundException.class, () -> f.bookingService.book(
                new BookingRequest(p.getId(), "nope", "A", null, f.request(p, 1, 2).stay(), 1)));
    }

    @Test
    void concurrentRequestsForLastRoomsNeverOverbook() throws Exception {
        int rooms = 3;
        int attempts = 30;
        Property p = f.propertyWithRooms(rooms);

        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch startGun = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            Callable<Boolean> task = () -> {
                startGun.await();
                try {
                    f.bookingService.book(f.request(p, 5, 8));
                    return true;
                } catch (NoAvailabilityException e) {
                    return false;
                }
            };
            results.add(pool.submit(task));
        }
        startGun.countDown();

        long successes = 0;
        for (Future<Boolean> r : results) {
            if (r.get()) successes++;
        }
        pool.shutdown();

        assertEquals(rooms, successes);
        assertEquals(rooms, f.bookingRepo.findByRoomTypeId(p.getRoomTypes().get(0).getId()).size());
    }
}
