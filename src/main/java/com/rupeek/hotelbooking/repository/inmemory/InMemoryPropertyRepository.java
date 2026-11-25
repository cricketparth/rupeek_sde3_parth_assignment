package com.rupeek.hotelbooking.repository.inmemory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.repository.PropertyRepository;

@Repository
public class InMemoryPropertyRepository implements PropertyRepository {
    private final Map<String, Property> store = new ConcurrentHashMap<>();

    @Override
    public Property save(Property property) {
        store.put(property.getId(), property);
        return property;
    }

    @Override
    public Optional<Property> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Property> findByCity(String city) {
        return store.values().stream()
                .filter(p -> p.getLocation().city().equalsIgnoreCase(city.trim()))
                .toList();
    }

    @Override
    public List<Property> findByOwnerId(String ownerId) {
        return store.values().stream().filter(p -> ownerId.equals(p.getOwnerId())).toList();
    }
}
