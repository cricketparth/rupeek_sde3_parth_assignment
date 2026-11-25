package com.rupeek.hotelbooking.booking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.common.InvalidStateException;
import com.rupeek.hotelbooking.common.NoAvailabilityException;
import com.rupeek.hotelbooking.common.NotFoundException;
import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.BookingStatus;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.domain.RoomType;
import com.rupeek.hotelbooking.inventory.AvailabilityService;
import com.rupeek.hotelbooking.inventory.InventoryLockManager;
import com.rupeek.hotelbooking.payment.PaymentService;
import com.rupeek.hotelbooking.pricing.PricingStrategy;
import com.rupeek.hotelbooking.repository.BookingRepository;
import com.rupeek.hotelbooking.repository.PropertyRepository;

/** Creates and cancels bookings. Availability check + save is atomic per room type (no double-booking). */
@Service
public class BookingService {

    private final PropertyRepository properties;
    private final BookingRepository bookings;
    private final AvailabilityService availability;
    private final InventoryLockManager locks;
    private final PricingStrategy pricing;
    private final CancellationPolicy cancellationPolicy;
    private final PaymentService paymentService;
    private final Clock clock;
    private final Duration holdDuration;

    public BookingService(PropertyRepository properties, BookingRepository bookings, AvailabilityService availability,
                          InventoryLockManager locks, PricingStrategy pricing, CancellationPolicy cancellationPolicy,
                          PaymentService paymentService, Clock clock, Duration bookingHoldDuration) {
        this.properties = properties;
        this.bookings = bookings;
        this.availability = availability;
        this.locks = locks;
        this.pricing = pricing;
        this.cancellationPolicy = cancellationPolicy;
        this.paymentService = paymentService;
        this.clock = clock;
        this.holdDuration = bookingHoldDuration;
    }

    public Booking book(BookingRequest request) {
        validate(request);
        Property property = properties.findById(request.propertyId())
                .orElseThrow(() -> new NotFoundException("Property", request.propertyId()));
        RoomType roomType = property.roomType(request.roomTypeId());
        if (request.guests() > roomType.getMaxGuests()) {
            throw new InvalidRequestException("Room type " + roomType.getName() + " sleeps at most " + roomType.getMaxGuests());
        }

        return locks.withLock(roomType.getId(), () -> {
            if (availability.availableUnits(roomType, request.stay()) <= 0) {
                throw new NoAvailabilityException("No " + roomType.getName() + " rooms available for the requested dates");
            }
            Instant now = clock.instant();
            Booking booking = new Booking(property.getId(), roomType.getId(), request.guestName().trim(),
                    request.guestEmail(), request.stay(), request.guests(),
                    pricing.priceFor(roomType, request.stay()), now, now.plus(holdDuration));
            return bookings.save(booking);
        });
    }

    public Booking get(String bookingId) {
        return bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking", bookingId));
    }

    /** Cancelling frees the inventory automatically because availability is derived from active bookings. */
    public Booking cancel(String bookingId) {
        Booking booking = get(bookingId);
        return locks.withLock(booking.getRoomTypeId(), () -> {
            Instant now = clock.instant();
            Instant checkIn = booking.getStay().checkIn().atStartOfDay(clock.getZone()).toInstant();
            BookingStatus before = booking.getStatus();

            if (before == BookingStatus.CONFIRMED && !now.isBefore(checkIn)) {
                throw new InvalidStateException("Cannot cancel a booking after check-in has started");
            }
            Money refund = before == BookingStatus.CONFIRMED
                    ? cancellationPolicy.refundFor(booking, checkIn, now)
                    : Money.ZERO;

            booking.cancel(refund, now); // throws if the current state forbids cancelling
            bookings.save(booking);
            if (refund.isPositive()) {
                paymentService.refund(bookingId, refund);
            }
            return booking;
        });
    }

    private void validate(BookingRequest r) {
        if (r.guestName() == null || r.guestName().isBlank()) throw new InvalidRequestException("guestName is required");
        if (r.stay() == null) throw new InvalidRequestException("checkIn and checkOut are required");
        if (r.guests() < 1) throw new InvalidRequestException("guests must be at least 1");
        if (r.stay().checkIn().isBefore(LocalDate.now(clock))) {
            throw new InvalidRequestException("checkIn cannot be in the past");
        }
    }
}
