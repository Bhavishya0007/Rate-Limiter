package com.ratelimiter.core;

public enum RateLimiterType {
    TOKEN_BUCKET,
    LEAKY_BUCKET,
    FIXED_WINDOW_COUNTER,
    SLIDING_WINDOW_LOG
}
