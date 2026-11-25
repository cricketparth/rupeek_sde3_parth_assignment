package com.rupeek.hotelbooking.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.rupeek.hotelbooking.common.InvalidRequestException;

/**
 * Account that owns one or more properties. A standalone property is simply an owner with exactly
 * one property; a chain is an owner with many. There is no separate "single property" code path.
 */
public final class Owner {
    private final String id;
    private final String name;
    private final List<String> propertyIds = new ArrayList<>();

    public Owner(String name) {
        if (name == null || name.isBlank()) throw new InvalidRequestException("owner name is required");
        this.id = UUID.randomUUID().toString();
        this.name = name.trim();
    }

    public synchronized void addProperty(String propertyId) {
        propertyIds.add(propertyId);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public synchronized List<String> getPropertyIds() { return Collections.unmodifiableList(new ArrayList<>(propertyIds)); }
}
