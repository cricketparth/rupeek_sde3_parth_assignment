package com.rupeek.hotelbooking.common;

/** Input failed business validation (HTTP 400). */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
