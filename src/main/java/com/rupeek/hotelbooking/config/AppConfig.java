package com.rupeek.hotelbooking.config;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** Injected everywhere time matters so tests can control it. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** How long an unpaid (PENDING_PAYMENT) booking keeps its inventory held. */
    @Bean
    public Duration bookingHoldDuration(@Value("${hotel.booking.hold-minutes:15}") long minutes) {
        return Duration.ofMinutes(minutes);
    }
}
