package com.rupeek.hotelbooking.pricing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.RoomType;

/** Default: base nightly rate x number of nights. */
@Component
@ConditionalOnProperty(name = "hotel.pricing.strategy", havingValue = "base", matchIfMissing = true)
public class PerNightPricingStrategy implements PricingStrategy {

    @Override
    public Money priceFor(RoomType roomType, DateRange stay) {
        return roomType.getBasePricePerNight().times(stay.nights());
    }
}
