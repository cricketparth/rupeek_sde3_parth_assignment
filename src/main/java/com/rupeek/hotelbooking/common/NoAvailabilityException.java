package com.rupeek.hotelbooking.common;

/** No inventory left for the requested room type and dates (HTTP 409). */
public class NoAvailabilityException extends RuntimeException {
    public NoAvailabilityException(String message) {
        super(message);
    }
}
