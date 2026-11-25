package com.rupeek.hotelbooking.inventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

/**
 * Serialises check-then-act operations per room type, so two concurrent requests can never both
 * see the last free room. Different room types never contend with each other.
 *
 * <p>Single-JVM only; with a real database this becomes a row lock / optimistic version check.
 */
@Component
public class InventoryLockManager {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T withLock(String roomTypeId, Supplier<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(roomTypeId, id -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
