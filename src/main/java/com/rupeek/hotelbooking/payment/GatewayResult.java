package com.rupeek.hotelbooking.payment;

public record GatewayResult(boolean success, String reference, String message) {
    public static GatewayResult ok(String reference) {
        return new GatewayResult(true, reference, "OK");
    }

    public static GatewayResult failed(String message) {
        return new GatewayResult(false, null, message);
    }
}
