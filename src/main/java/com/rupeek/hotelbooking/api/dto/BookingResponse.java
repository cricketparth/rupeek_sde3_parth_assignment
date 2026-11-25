package com.rupeek.hotelbooking.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.BookingStatus;

public record BookingResponse(String id, String propertyId, String roomTypeId, String guestName, LocalDate checkIn,
                              LocalDate checkOut, int guests, BigDecimal totalAmount, BookingStatus status,
                              Instant holdExpiresAt, BigDecimal refundAmount) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getPropertyId(), b.getRoomTypeId(), b.getGuestName(),
                b.getStay().checkIn(), b.getStay().checkOut(), b.getGuests(), b.getTotalAmount().amount(),
                b.getStatus(), b.getHoldExpiresAt(), b.getRefundAmount().amount());
    }
}
