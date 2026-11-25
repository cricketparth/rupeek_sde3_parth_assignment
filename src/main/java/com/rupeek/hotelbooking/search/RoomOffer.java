package com.rupeek.hotelbooking.search;

import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.RoomType;

/** A room type that is free for the searched dates, with its price for the whole stay. */
public record RoomOffer(RoomType roomType, int availableUnits, Money totalPrice, Money averagePricePerNight) {
}
