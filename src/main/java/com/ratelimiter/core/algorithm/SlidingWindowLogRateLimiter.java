package com.ratelimiter.core.algorithm;

import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.core.RateLimiterConfig;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Keeps a timestamp log of accepted requests and evicts entries older
 * than the sliding window on every check. Precise, but memory grows with
 * request volume within a window.
 */
public class SlidingWindowLogRateLimiter implements RateLimiter {

    private final int capacity;
    private final long windowSizeMillis;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();
    private final Deque<Long> requestLog = new ArrayDeque<>();

    public SlidingWindowLogRateLimiter(RateLimiterConfig config, Clock clock) {
        this.capacity = config.getCapacity();
        this.windowSizeMillis = config.getWindowSizeMillis();
        this.clock = clock;
    }

    public SlidingWindowLogRateLimiter(RateLimiterConfig config) {
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
        if (permits > capacity) {
            return false;
        }
        lock.lock();
        try {
            long now = clock.millis();
            long windowStart = now - windowSizeMillis;
            while (!requestLog.isEmpty() && requestLog.peekFirst() <= windowStart) {
                requestLog.pollFirst();
            }
            if (requestLog.size() + permits <= capacity) {
                for (int i = 0; i < permits; i++) {
                    requestLog.addLast(now);
                }
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
}
