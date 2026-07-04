package com.ratelimiter.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test-only {@link Clock} whose instant can be advanced manually, so
 * algorithm tests are deterministic instead of relying on Thread.sleep.
 */
public class MutableClock extends Clock {

    private final AtomicReference<Instant> instant;
    private final ZoneId zone;

    public MutableClock(Instant initial) {
        this(initial, ZoneId.systemDefault());
    }

    private MutableClock(Instant initial, ZoneId zone) {
        this.instant = new AtomicReference<>(initial);
        this.zone = zone;
    }

    public void advance(long amount, TimeUnit unit) {
        instant.updateAndGet(current -> current.plusNanos(unit.toNanos(amount)));
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant.get(), zone);
    }

    @Override
    public Instant instant() {
        return instant.get();
    }
}
