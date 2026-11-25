package com.rupeek.hotelbooking.repository.inmemory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.rupeek.hotelbooking.domain.Owner;
import com.rupeek.hotelbooking.repository.OwnerRepository;

@Repository
public class InMemoryOwnerRepository implements OwnerRepository {
    private final Map<String, Owner> store = new ConcurrentHashMap<>();

    @Override
    public Owner save(Owner owner) {
        store.put(owner.getId(), owner);
        return owner;
    }

    @Override
    public Optional<Owner> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
