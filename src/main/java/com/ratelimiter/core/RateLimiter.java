package com.ratelimiter.core;

/**
 * Strategy interface implemented by every rate-limiting algorithm.
 * Implementations must be thread-safe: a single instance is shared
 * across concurrent requests for the same client/policy.
 */
public interface RateLimiter {

    boolean tryAcquire();

    boolean tryAcquire(int permits);
}
