package com.rupeek.hotelbooking.pricing;

import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.RoomType;

/** Seam for pricing rules (seasonal, weekend, demand-based...). Returns the total for the whole stay. */
public interface PricingStrategy {
    Money priceFor(RoomType roomType, DateRange stay);
}
