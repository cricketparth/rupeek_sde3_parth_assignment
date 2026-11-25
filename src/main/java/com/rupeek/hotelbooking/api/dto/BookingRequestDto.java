package com.rupeek.hotelbooking.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingRequestDto(@NotBlank String propertyId, @NotBlank String roomTypeId,
                                @NotBlank String guestName, @Email String guestEmail,
                                @NotNull LocalDate checkIn, @NotNull LocalDate checkOut, @Min(1) int guests) {
}
