package com.rupeek.hotelbooking.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rupeek.hotelbooking.api.dto.OwnerRequest;
import com.rupeek.hotelbooking.api.dto.OwnerRequest.PropertyRequest;
import com.rupeek.hotelbooking.api.dto.OwnerResponse;
import com.rupeek.hotelbooking.api.dto.PropertyResponse;
import com.rupeek.hotelbooking.domain.Location;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Owner;
import com.rupeek.hotelbooking.property.PropertyDetails;
import com.rupeek.hotelbooking.property.PropertyDetails.RoomTypeDetails;
import com.rupeek.hotelbooking.property.PropertyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/owners")
public class OwnerController {

    private final PropertyService propertyService;

    public OwnerController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OwnerResponse register(@Valid @RequestBody OwnerRequest request) {
        List<PropertyDetails> details = request.properties() == null ? List.of()
                : request.properties().stream().map(OwnerController::toDetails).toList();
        Owner owner = propertyService.registerOwner(request.name(), details);
        return OwnerResponse.from(owner, propertyService.propertiesOf(owner.getId()));
    }

    @GetMapping("/{ownerId}")
    public OwnerResponse get(@PathVariable String ownerId) {
        return OwnerResponse.from(propertyService.getOwner(ownerId), propertyService.propertiesOf(ownerId));
    }

    @PostMapping("/{ownerId}/properties")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse addProperty(@PathVariable String ownerId, @Valid @RequestBody PropertyRequest request) {
        return PropertyResponse.from(propertyService.addProperty(ownerId, toDetails(request)));
    }

    private static PropertyDetails toDetails(PropertyRequest p) {
        return new PropertyDetails(p.name(), p.type(), new Location(p.city(), p.locality()), p.starRating(),
                p.amenities(), p.roomTypes().stream()
                .map(r -> new RoomTypeDetails(r.name(), r.maxGuests(), Money.of(r.pricePerNight()), r.totalRooms()))
                .toList());
    }
}
