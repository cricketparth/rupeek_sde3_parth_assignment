# Hotel Booking System

Spring Boot (Java 17+) backend for a hotel booking platform: property onboarding, discovery with
pluggable filters, booking with double-booking protection, mock payments and cancellation with refunds.

## Build, test, run

```bash
mvn test                 # 45 unit + API tests
mvn spring-boot:run      # starts on http://localhost:8080
```

Swagger UI: http://localhost:8080/swagger-ui/index.html (OpenAPI JSON at `/v3/api-docs`).
Storage is in-memory, so data resets on restart.

## API

| Operation | Endpoint |
|---|---|
| Onboard owner (+ 0..n properties) | `POST /api/v1/owners` |
| Add property to existing owner | `POST /api/v1/owners/{ownerId}/properties` |
| Search | `GET /api/v1/properties/search?city=&checkIn=&checkOut=&guests=[&locality=&minPrice=&maxPrice=&minStars=&amenities=POOL,WIFI]` |
| Book (creates a held booking) | `POST /api/v1/bookings` |
| Pay (header `Idempotency-Key` optional) | `POST /api/v1/bookings/{id}/payments` |
| Cancel | `POST /api/v1/bookings/{id}/cancel` |
| Get booking / property / owner | `GET /api/v1/bookings/{id}`, `/properties/{id}`, `/owners/{id}` |

Quick walkthrough:

```bash
curl -X POST localhost:8080/api/v1/owners -H 'Content-Type: application/json' -d '{
  "name":"Taj Group","properties":[{"name":"Taj Bengaluru","type":"HOTEL","city":"Bengaluru","locality":"MG Road",
  "starRating":5,"amenities":["WIFI","POOL"],
  "roomTypes":[{"name":"Deluxe","maxGuests":2,"pricePerNight":5000,"totalRooms":10}]}]}'

curl "localhost:8080/api/v1/properties/search?city=Bengaluru&checkIn=2030-01-10&checkOut=2030-01-12&guests=2&amenities=POOL"

curl -X POST localhost:8080/api/v1/bookings -H 'Content-Type: application/json' -d '{
  "propertyId":"<id>","roomTypeId":"<id>","guestName":"Asha","guestEmail":"asha@example.com",
  "checkIn":"2030-01-10","checkOut":"2030-01-12","guests":2}'

curl -X POST localhost:8080/api/v1/bookings/<bookingId>/payments -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: abc-1' -d '{"method":"CARD","details":{"cardNumber":"4111111111111111"}}'

curl -X POST localhost:8080/api/v1/bookings/<bookingId>/cancel
```

Payment details by method: `CARD` -> `cardNumber`, `UPI` -> `vpa`, `WALLET` -> `walletId`.
Add `"simulateFailure":"true"` to `details` to make the mock gateway decline.

## Design

```
api         REST controllers, DTOs, error mapping        (framework edge only)
domain      Owner, Property, RoomType, Booking, Payment, Money, DateRange   (no Spring)
property    onboarding use case
search      SearchService + SearchFilter implementations
inventory   AvailabilityService, InventoryLockManager
booking     BookingService, CancellationPolicy
payment     PaymentService, PaymentProcessor implementations
pricing     PricingStrategy implementations
repository  interfaces + inmemory/ implementations
```

Extension points (each is "add one class", no edits to existing logic):

| To add... | Do this |
|---|---|
| A search filter | `@Component` implementing `SearchFilter`; `SearchService` auto-discovers it |
| A payment method | Add enum value + `@Component` implementing `PaymentProcessor` |
| A cancellation policy | Implement `CancellationPolicy` (current default: `TieredCancellationPolicy`) |
| A pricing rule | Implement `PricingStrategy`; select via `hotel.pricing.strategy` (`base` or `weekend`) |
| A property type / amenity | Add an enum value |
| A database | Implement the `repository` interfaces |

### Key decisions

- **Single property is a special case of a chain.** Everything belongs to an `Owner`; a standalone
  property is an owner with one property. One onboarding path, no branching.
- **Availability is derived, not stored.** `available = totalRooms - bookings that hold inventory and
  overlap the dates`. So cancel/expire/payment-failure release inventory with no extra bookkeeping
  and no counter that can drift.
- **Stays are half-open `[checkIn, checkOut)`**, so back-to-back stays on the same room are allowed.
- **Booking lifecycle** (`BookingStatus` owns the legal transitions; `Booking` enforces them):
  `PENDING_PAYMENT -> CONFIRMED | PAYMENT_FAILED | EXPIRED | CANCELLED`, `CONFIRMED -> CANCELLED`.
- **Hold + expiry.** Booking creates a `PENDING_PAYMENT` hold (default 15 min, `hotel.booking.hold-minutes`).
  An unpaid hold stops counting as inventory once it lapses (evaluated lazily against the injected
  `Clock`, no scheduler needed). Paying an expired hold is rejected.
- **Concurrency.** The check-availability-then-save step runs under a per-room-type lock
  (`InventoryLockManager`), so N concurrent requests for the last room produce exactly one booking
  (covered by a 30-thread test). Payment confirmation and cancellation take the same lock, so a lapsed hold
  can never be confirmed after its room has been re-sold. Different room types never contend.
- **Payment outcome drives booking state**: success -> `CONFIRMED`, decline -> `PAYMENT_FAILED`
  (inventory released). Payment is idempotent per `(booking, Idempotency-Key)`.
- **Third party mocked behind `PaymentProcessor`.** Refunds go back through the same processor.
- **Money** is a `BigDecimal` value object (2dp, single implicit currency).

### Assumptions

- One room unit per booking; `guests` must fit within the room type's `maxGuests`.
- Search requires city + dates + guests; the price filter compares the average nightly price.
- Check-in time is start of day in the server's time zone. Refund tiers: 100% at 48h+ before check-in,
  50% at 24h+, else 0. Confirmed bookings cannot be cancelled once check-in has started.
- No auth, no UI, no real gateway (out of scope per brief).

### What I would do with more time

- Real database: unique/exclusion constraint or row lock / optimistic versioning instead of the in-JVM lock
  (which is single-instance only), plus availability stored per room-type per night for faster search.
- Scheduled job to flip lapsed holds to `EXPIRED`, pagination/sorting options on search, multi-room bookings.
- Payment webhooks + async confirmation, persisted idempotency keys with TTL, currency support.
- Property-level cancellation policy configuration, richer pricing (seasonal/demand), more API tests.
