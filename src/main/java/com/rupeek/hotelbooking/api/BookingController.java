package com.rupeek.hotelbooking.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rupeek.hotelbooking.api.dto.BookingRequestDto;
import com.rupeek.hotelbooking.api.dto.BookingResponse;
import com.rupeek.hotelbooking.api.dto.PaymentRequestDto;
import com.rupeek.hotelbooking.api.dto.PaymentResponse;
import com.rupeek.hotelbooking.booking.BookingRequest;
import com.rupeek.hotelbooking.booking.BookingService;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.payment.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    public BookingController(BookingService bookingService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@Valid @RequestBody BookingRequestDto dto) {
        BookingRequest request = new BookingRequest(dto.propertyId(), dto.roomTypeId(), dto.guestName(),
                dto.guestEmail(), new DateRange(dto.checkIn(), dto.checkOut()), dto.guests());
        return BookingResponse.from(bookingService.book(request));
    }

    @GetMapping("/{bookingId}")
    public BookingResponse get(@PathVariable String bookingId) {
        return BookingResponse.from(bookingService.get(bookingId));
    }

    /** Send an Idempotency-Key header to make retries safe: the same key never charges twice. */
    @PostMapping("/{bookingId}/payments")
    public PaymentResponse pay(@PathVariable String bookingId,
                               @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                               @Valid @RequestBody PaymentRequestDto dto) {
        return PaymentResponse.from(paymentService.pay(bookingId, dto.method(), dto.details(), idempotencyKey));
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancel(@PathVariable String bookingId) {
        return BookingResponse.from(bookingService.cancel(bookingId));
    }
}
