package com.ratelimiter.core.factory;

import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.core.RateLimiterConfig;
import com.ratelimiter.core.algorithm.FixedWindowCounterRateLimiter;
import com.ratelimiter.core.algorithm.LeakyBucketRateLimiter;
import com.ratelimiter.core.algorithm.SlidingWindowLogRateLimiter;
import com.ratelimiter.core.algorithm.TokenBucketRateLimiter;

import java.time.Clock;

/**
 * Selects the concrete {@link RateLimiter} strategy for a given
 * {@link RateLimiterConfig}. Callers depend only on this factory and the
 * {@link RateLimiter} interface, never on a specific algorithm class.
 */
public final class RateLimiterFactory {

    private RateLimiterFactory() {
    }

    public static RateLimiter create(RateLimiterConfig config) {
        return create(config, Clock.systemUTC());
    }

    public static RateLimiter create(RateLimiterConfig config, Clock clock) {
        return switch (config.getType()) {
            case TOKEN_BUCKET -> new TokenBucketRateLimiter(config, clock);
            case LEAKY_BUCKET -> new LeakyBucketRateLimiter(config, clock);
            case FIXED_WINDOW_COUNTER -> new FixedWindowCounterRateLimiter(config, clock);
            case SLIDING_WINDOW_LOG -> new SlidingWindowLogRateLimiter(config, clock);
        };
    }
}
