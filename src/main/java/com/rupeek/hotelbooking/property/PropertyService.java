package com.rupeek.hotelbooking.property;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rupeek.hotelbooking.common.InvalidRequestException;
import com.rupeek.hotelbooking.common.NotFoundException;
import com.rupeek.hotelbooking.domain.Owner;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.repository.OwnerRepository;
import com.rupeek.hotelbooking.repository.PropertyRepository;

/** Onboarding. A standalone property and a chain share one path: an owner with N >= 1 properties. */
@Service
public class PropertyService {

    private final OwnerRepository owners;
    private final PropertyRepository properties;

    public PropertyService(OwnerRepository owners, PropertyRepository properties) {
        this.owners = owners;
        this.properties = properties;
    }

    public Owner registerOwner(String name, List<PropertyDetails> initialProperties) {
        Owner owner = owners.save(new Owner(name));
        if (initialProperties != null) {
            initialProperties.forEach(details -> addProperty(owner.getId(), details));
        }
        return owner;
    }

    public Property addProperty(String ownerId, PropertyDetails details) {
        Owner owner = getOwner(ownerId);
        if (details.roomTypes() == null || details.roomTypes().isEmpty()) {
            throw new InvalidRequestException("a property needs at least one room type");
        }
        Property property = new Property(ownerId, details.name(), details.type(), details.location(),
                details.starRating(), details.amenities());
        details.roomTypes().forEach(rt ->
                property.addRoomType(rt.name(), rt.maxGuests(), rt.pricePerNight(), rt.totalRooms()));
        properties.save(property);
        owner.addProperty(property.getId());
        return property;
    }

    public Owner getOwner(String ownerId) {
        return owners.findById(ownerId).orElseThrow(() -> new NotFoundException("Owner", ownerId));
    }

    public Property getProperty(String propertyId) {
        return properties.findById(propertyId).orElseThrow(() -> new NotFoundException("Property", propertyId));
    }

    public List<Property> propertiesOf(String ownerId) {
        getOwner(ownerId);
        return properties.findByOwnerId(ownerId);
    }
}
