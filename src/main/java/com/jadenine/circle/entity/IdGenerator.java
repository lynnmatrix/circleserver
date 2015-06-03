package com.jadenine.circle.entity;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by linym on 6/3/15.
 */
public class IdGenerator{
    private final AtomicLong nextId;

    public IdGenerator(long currentMaxId) {
        nextId = new AtomicLong(currentMaxId + 1);
    }

    public long getAndIncrement() {
        return nextId.getAndIncrement();
    }
}
