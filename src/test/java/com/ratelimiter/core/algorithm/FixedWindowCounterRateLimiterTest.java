package com.ratelimiter.core.algorithm;

import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.core.RateLimiterConfig;
import com.ratelimiter.core.RateLimiterType;
import com.ratelimiter.util.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowCounterRateLimiterTest {

    private MutableClock clock;
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimiterConfig config = RateLimiterConfig.builder(RateLimiterType.FIXED_WINDOW_COUNTER)
                .capacity(3)
                .windowSizeMillis(1000)
                .build();
        rateLimiter = new FixedWindowCounterRateLimiter(config, clock);
    }

    @Test
    void allowsRequestsUpToCapacityWithinWindow() {
        assertTrue(rateLimiter.tryAcquire());
        assertTrue(rateLimiter.tryAcquire());
        assertTrue(rateLimiter.tryAcquire());
        assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    void resetsCountInNewWindow() {
        assertTrue(rateLimiter.tryAcquire());
        assertTrue(rateLimiter.tryAcquire());
        assertTrue(rateLimiter.tryAcquire());
        assertFalse(rateLimiter.tryAcquire());

        clock.advance(1100, TimeUnit.MILLISECONDS);

        assertTrue(rateLimiter.tryAcquire());
    }
}
