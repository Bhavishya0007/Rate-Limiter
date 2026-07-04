package com.ratelimiter.core;

import java.util.Objects;

/**
 * Immutable configuration consumed by {@link com.ratelimiter.core.factory.RateLimiterFactory}.
 * Not every field applies to every algorithm:
 *  - capacity: TOKEN_BUCKET, LEAKY_BUCKET, FIXED_WINDOW_COUNTER, SLIDING_WINDOW_LOG
 *  - ratePerSecond: TOKEN_BUCKET (refill rate), LEAKY_BUCKET (leak rate)
 *  - windowSizeMillis: FIXED_WINDOW_COUNTER, SLIDING_WINDOW_LOG
 */
public final class RateLimiterConfig {

    private final RateLimiterType type;
    private final int capacity;
    private final double ratePerSecond;
    private final long windowSizeMillis;

    private RateLimiterConfig(Builder builder) {
        this.type = builder.type;
        this.capacity = builder.capacity;
        this.ratePerSecond = builder.ratePerSecond;
        this.windowSizeMillis = builder.windowSizeMillis;
    }

    public RateLimiterType getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getRatePerSecond() {
        return ratePerSecond;
    }

    public long getWindowSizeMillis() {
        return windowSizeMillis;
    }

    public static Builder builder(RateLimiterType type) {
        return new Builder(type);
    }

    public static final class Builder {
        private final RateLimiterType type;
        private int capacity = 10;
        private double ratePerSecond = 1.0;
        private long windowSizeMillis = 1000L;

        private Builder(RateLimiterType type) {
            this.type = Objects.requireNonNull(type, "type must not be null");
        }

        public Builder capacity(int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("capacity must be positive");
            }
            this.capacity = capacity;
            return this;
        }

        public Builder ratePerSecond(double ratePerSecond) {
            if (ratePerSecond <= 0) {
                throw new IllegalArgumentException("ratePerSecond must be positive");
            }
            this.ratePerSecond = ratePerSecond;
            return this;
        }

        public Builder windowSizeMillis(long windowSizeMillis) {
            if (windowSizeMillis <= 0) {
                throw new IllegalArgumentException("windowSizeMillis must be positive");
            }
            this.windowSizeMillis = windowSizeMillis;
            return this;
        }

        public RateLimiterConfig build() {
            return new RateLimiterConfig(this);
        }
    }
}
