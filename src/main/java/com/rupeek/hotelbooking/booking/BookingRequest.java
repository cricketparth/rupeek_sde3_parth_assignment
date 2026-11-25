package com.rupeek.hotelbooking.booking;

import com.rupeek.hotelbooking.domain.DateRange;

public record BookingRequest(String propertyId, String roomTypeId, String guestName, String guestEmail,
                             DateRange stay, int guests) {
}
