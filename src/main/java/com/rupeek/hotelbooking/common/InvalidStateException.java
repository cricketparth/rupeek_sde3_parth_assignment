package com.rupeek.hotelbooking.common;

/** Operation is not allowed in the entity's current state (HTTP 409). */
public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String message) {
        super(message);
    }
}
