package com.rupeek.hotelbooking.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import com.rupeek.hotelbooking.common.InvalidRequestException;

/** Half-open stay interval [checkIn, checkOut): the check-out day is not an occupied night. */
public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    public DateRange {
        if (checkIn == null || checkOut == null) {
            throw new InvalidRequestException("checkIn and checkOut are required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidRequestException("checkOut must be after checkIn");
        }
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** Back-to-back stays (one checks out the day the next checks in) do not overlap. */
    public boolean overlaps(DateRange other) {
        return checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
    }

    /** Each occupied night, i.e. every date in [checkIn, checkOut). */
    public List<LocalDate> eachNight() {
        return Stream.iterate(checkIn, d -> d.plusDays(1)).limit(nights()).toList();
    }
}
