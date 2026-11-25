package com.rupeek.hotelbooking.api.dto;

import java.math.BigDecimal;
import java.util.List;

import com.rupeek.hotelbooking.search.PropertyCandidate;
import com.rupeek.hotelbooking.search.RoomOffer;

public record SearchResultResponse(PropertyResponse property, List<OfferResponse> offers) {

    public record OfferResponse(String roomTypeId, String roomTypeName, int maxGuests, int availableUnits,
                                BigDecimal totalPrice, BigDecimal averagePricePerNight) {
        static OfferResponse from(RoomOffer o) {
            return new OfferResponse(o.roomType().getId(), o.roomType().getName(), o.roomType().getMaxGuests(),
                    o.availableUnits(), o.totalPrice().amount(), o.averagePricePerNight().amount());
        }
    }

    public static SearchResultResponse from(PropertyCandidate c) {
        return new SearchResultResponse(PropertyResponse.from(c.property()),
                c.offers().stream().map(OfferResponse::from).toList());
    }
}
