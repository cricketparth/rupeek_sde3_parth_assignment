package com.rupeek.hotelbooking.api.dto;

import java.util.List;

import com.rupeek.hotelbooking.domain.Owner;
import com.rupeek.hotelbooking.domain.Property;

public record OwnerResponse(String id, String name, List<PropertyResponse> properties) {
    public static OwnerResponse from(Owner owner, List<Property> properties) {
        return new OwnerResponse(owner.getId(), owner.getName(), properties.stream().map(PropertyResponse::from).toList());
    }
}
