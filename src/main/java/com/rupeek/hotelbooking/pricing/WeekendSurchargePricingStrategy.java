package com.rupeek.hotelbooking.pricing;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.RoomType;

/** Example dynamic pricing: +25% on Friday and Saturday nights. Enable with hotel.pricing.strategy=weekend. */
@Component
@ConditionalOnProperty(name = "hotel.pricing.strategy", havingValue = "weekend")
public class WeekendSurchargePricingStrategy implements PricingStrategy {

    private static final int SURCHARGE_PERCENT = 25;

    @Override
    public Money priceFor(RoomType roomType, DateRange stay) {
        Money total = Money.ZERO;
        for (LocalDate night : stay.eachNight()) {
            Money nightly = roomType.getBasePricePerNight();
            if (isWeekend(night)) {
                nightly = nightly.plus(nightly.percent(SURCHARGE_PERCENT));
            }
            total = total.plus(nightly);
        }
        return total;
    }

    private boolean isWeekend(LocalDate night) {
        DayOfWeek d = night.getDayOfWeek();
        return d == DayOfWeek.FRIDAY || d == DayOfWeek.SATURDAY;
    }
}
