package com.rupeek.hotelbooking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.rupeek.hotelbooking.common.InvalidRequestException;

class DateRangeTest {

    private static DateRange range(int from, int to) {
        LocalDate base = LocalDate.of(2030, 1, 1);
        return new DateRange(base.plusDays(from), base.plusDays(to));
    }

    @Test
    void countsNights() {
        assertEquals(3, range(0, 3).nights());
        assertEquals(3, range(0, 3).eachNight().size());
    }

    @Test
    void rejectsCheckOutNotAfterCheckIn() {
        assertThrows(InvalidRequestException.class, () -> range(2, 2));
        assertThrows(InvalidRequestException.class, () -> range(3, 1));
    }

    @Test
    void overlappingRangesDetected() {
        assertTrue(range(0, 3).overlaps(range(2, 5)));
        assertTrue(range(0, 5).overlaps(range(1, 2)));
    }

    @Test
    void backToBackStaysDoNotOverlap() {
        assertFalse(range(0, 3).overlaps(range(3, 5)));
        assertFalse(range(3, 5).overlaps(range(0, 3)));
    }
}
