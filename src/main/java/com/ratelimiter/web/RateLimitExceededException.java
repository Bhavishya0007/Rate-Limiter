package com.ratelimiter.web;

/**
 * Thrown when a client has exhausted its rate limit for the resolved
 * policy; translated to an HTTP 429 by the Javalin exception handler.
 */
public class RateLimitExceededException extends RuntimeException {

    private final String policy;

    public RateLimitExceededException(String policy) {
        super("Rate limit exceeded for policy: " + policy);
        this.policy = policy;
    }

    public String getPolicy() {
        return policy;
    }
}
