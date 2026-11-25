package com.rupeek.hotelbooking.repository;

import java.util.Optional;

import com.rupeek.hotelbooking.domain.Owner;

public interface OwnerRepository {
    Owner save(Owner owner);
    Optional<Owner> findById(String id);
}
