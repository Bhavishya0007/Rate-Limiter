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

class LeakyBucketRateLimiterTest {

    private MutableClock clock;
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimiterConfig config = RateLimiterConfig.builder(RateLimiterType.LEAKY_BUCKET)
                .capacity(5)
                .ratePerSecond(1)
                .build();
        rateLimiter = new LeakyBucketRateLimiter(config, clock);
    }

    @Test
    void allowsRequestsUpToCapacity() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryAcquire());
        }
        assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    void leaksOverTimeFreeingCapacity() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.tryAcquire();
        }
        assertFalse(rateLimiter.tryAcquire());

        clock.advance(2, TimeUnit.SECONDS);

        assertTrue(rateLimiter.tryAcquire());
        assertTrue(rateLimiter.tryAcquire());
        assertFalse(rateLimiter.tryAcquire());
    }
}
