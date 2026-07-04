package com.ratelimiter.core.algorithm;

import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.core.RateLimiterConfig;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allows bursts up to {@code capacity}, refilling tokens continuously at
 * {@code ratePerSecond}. Good default choice for most APIs.
 */
public class TokenBucketRateLimiter implements RateLimiter {

    private final int capacity;
    private final double refillTokensPerNano;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(RateLimiterConfig config, Clock clock) {
        this.capacity = config.getCapacity();
        this.refillTokensPerNano = config.getRatePerSecond() / 1_000_000_000.0;
        this.clock = clock;
        this.availableTokens = capacity;
        this.lastRefillNanos = nowNanos();
    }

    public TokenBucketRateLimiter(RateLimiterConfig config) {
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
            refill();
            if (availableTokens >= permits) {
                availableTokens -= permits;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void refill() {
        long now = nowNanos();
        long elapsed = now - lastRefillNanos;
        if (elapsed <= 0) {
            return;
        }
        double refreshed = elapsed * refillTokensPerNano;
        availableTokens = Math.min(capacity, availableTokens + refreshed);
        lastRefillNanos = now;
    }

    private long nowNanos() {
        Instant instant = clock.instant();
        return instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
    }
}
