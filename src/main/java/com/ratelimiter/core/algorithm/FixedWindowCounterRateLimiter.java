package com.ratelimiter.core.algorithm;

import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.core.RateLimiterConfig;

import java.time.Clock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Counts requests within a fixed time window and resets the counter when
 * the window elapses. Simplest algorithm, but allows up to 2x capacity
 * bursts across a window boundary.
 */
public class FixedWindowCounterRateLimiter implements RateLimiter {

    private final int capacity;
    private final long windowSizeMillis;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    private long currentWindowStart;
    private int count;

    public FixedWindowCounterRateLimiter(RateLimiterConfig config, Clock clock) {
        this.capacity = config.getCapacity();
        this.windowSizeMillis = config.getWindowSizeMillis();
        this.clock = clock;
        this.currentWindowStart = clock.millis();
        this.count = 0;
    }

    public FixedWindowCounterRateLimiter(RateLimiterConfig config) {
        this(config, Clock.systemUTC());
    }

    @Override
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    @Override
    public boolean tryAcquire(int permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }
        lock.lock();
        try {
            long now = clock.millis();
            if (now - currentWindowStart >= windowSizeMillis) {
                currentWindowStart = now;
                count = 0;
            }
            if (count + permits <= capacity) {
                count += permits;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
}
