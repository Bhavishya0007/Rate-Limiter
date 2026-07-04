package com.ratelimiter.core.algorithm;

import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.core.RateLimiterConfig;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Models a bucket that fills with each request and drains at a fixed
 * {@code ratePerSecond}, smoothing bursty traffic to a constant outflow.
 */
public class LeakyBucketRateLimiter implements RateLimiter {

    private final int capacity;
    private final double leakPerNano;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    private double currentLevel;
    private long lastLeakNanos;

    public LeakyBucketRateLimiter(RateLimiterConfig config, Clock clock) {
        this.capacity = config.getCapacity();
        this.leakPerNano = config.getRatePerSecond() / 1_000_000_000.0;
        this.clock = clock;
        this.currentLevel = 0;
        this.lastLeakNanos = nowNanos();
    }

    public LeakyBucketRateLimiter(RateLimiterConfig config) {
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
            leak();
            if (currentLevel + permits <= capacity) {
                currentLevel += permits;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void leak() {
        long now = nowNanos();
        long elapsed = now - lastLeakNanos;
        if (elapsed <= 0) {
            return;
        }
        double leaked = elapsed * leakPerNano;
        currentLevel = Math.max(0, currentLevel - leaked);
        lastLeakNanos = now;
    }

    private long nowNanos() {
        Instant instant = clock.instant();
        return instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
    }
}
