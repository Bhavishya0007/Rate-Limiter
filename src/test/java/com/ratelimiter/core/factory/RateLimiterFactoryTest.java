package com.ratelimiter.core.factory;

import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.core.RateLimiterConfig;
import com.ratelimiter.core.RateLimiterType;
import com.ratelimiter.core.algorithm.FixedWindowCounterRateLimiter;
import com.ratelimiter.core.algorithm.LeakyBucketRateLimiter;
import com.ratelimiter.core.algorithm.SlidingWindowLogRateLimiter;
import com.ratelimiter.core.algorithm.TokenBucketRateLimiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RateLimiterFactoryTest {

    @Test
    void createsTokenBucketForTokenBucketType() {
        RateLimiterConfig config = RateLimiterConfig.builder(RateLimiterType.TOKEN_BUCKET)
                .capacity(1).ratePerSecond(1).build();
        RateLimiter limiter = RateLimiterFactory.create(config);
        assertInstanceOf(TokenBucketRateLimiter.class, limiter);
    }

    @Test
    void createsLeakyBucketForLeakyBucketType() {
        RateLimiterConfig config = RateLimiterConfig.builder(RateLimiterType.LEAKY_BUCKET)
                .capacity(1).ratePerSecond(1).build();
        RateLimiter limiter = RateLimiterFactory.create(config);
        assertInstanceOf(LeakyBucketRateLimiter.class, limiter);
    }

    @Test
    void createsFixedWindowForFixedWindowType() {
        RateLimiterConfig config = RateLimiterConfig.builder(RateLimiterType.FIXED_WINDOW_COUNTER)
                .capacity(1).windowSizeMillis(1000).build();
        RateLimiter limiter = RateLimiterFactory.create(config);
        assertInstanceOf(FixedWindowCounterRateLimiter.class, limiter);
    }

    @Test
    void createsSlidingWindowLogForSlidingWindowLogType() {
        RateLimiterConfig config = RateLimiterConfig.builder(RateLimiterType.SLIDING_WINDOW_LOG)
                .capacity(1).windowSizeMillis(1000).build();
        RateLimiter limiter = RateLimiterFactory.create(config);
        assertInstanceOf(SlidingWindowLogRateLimiter.class, limiter);
    }
}
