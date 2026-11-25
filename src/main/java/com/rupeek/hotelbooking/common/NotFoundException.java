package com.rupeek.hotelbooking.common;

/** Requested entity does not exist (HTTP 404). */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, String id) {
        super(entity + " not found: " + id);
    }
}
