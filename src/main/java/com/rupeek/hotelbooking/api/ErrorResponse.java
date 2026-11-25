package com.rupeek.hotelbooking.api;

public record ErrorResponse(int status, String error, String message) {
}
