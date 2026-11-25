package com.rupeek.hotelbooking.repository;

import java.util.List;
import java.util.Optional;

import com.rupeek.hotelbooking.domain.Property;

public interface PropertyRepository {
    Property save(Property property);
    Optional<Property> findById(String id);
    List<Property> findByCity(String city);
    List<Property> findByOwnerId(String ownerId);
}
