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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketRateLimiterTest {

    private MutableClock clock;
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimiterConfig config = RateLimiterConfig.builder(RateLimiterType.TOKEN_BUCKET)
                .capacity(5)
                .ratePerSecond(1)
                .build();
        rateLimiter = new TokenBucketRateLimiter(config, clock);
    }

    @Test
    void allowsRequestsUpToCapacity() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryAcquire(), "request " + i + " should be allowed");
        }
        assertFalse(rateLimiter.tryAcquire(), "request beyond capacity should be rejected");
    }

    @Test
    void refillsTokensOverTime() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.tryAcquire();
        }
        assertFalse(rateLimiter.tryAcquire());

        clock.advance(3, TimeUnit.SECONDS);

        assertTrue(rateLimiter.tryAcquire());
        assertTrue(rateLimiter.tryAcquire());
        assertTrue(rateLimiter.tryAcquire());
        assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    void rejectsNonPositivePermits() {
        assertThrows(IllegalArgumentException.class, () -> rateLimiter.tryAcquire(0));
    }
}
