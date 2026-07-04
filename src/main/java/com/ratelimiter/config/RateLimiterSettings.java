package com.ratelimiter.config;

import com.ratelimiter.core.RateLimiterType;

import java.util.Map;

/**
 * Immutable, framework-neutral view of {@code application.yml}: named
 * policies (algorithm + params) plus a route-pattern-to-policy mapping.
 */
public final class RateLimiterSettings {

    private final String defaultPolicy;
    private final Map<String, PolicyConfig> policies;
    private final Map<String, String> routes;

    public RateLimiterSettings(String defaultPolicy, Map<String, PolicyConfig> policies, Map<String, String> routes) {
        this.defaultPolicy = defaultPolicy;
        this.policies = policies;
        this.routes = routes;
    }

    public String getDefaultPolicy() {
        return defaultPolicy;
    }

    public Map<String, PolicyConfig> getPolicies() {
        return policies;
    }

    public Map<String, String> getRoutes() {
        return routes;
    }

    public static final class PolicyConfig {
        private final RateLimiterType type;
        private final int capacity;
        private final double ratePerSecond;
        private final long windowSizeMillis;

        public PolicyConfig(RateLimiterType type, int capacity, double ratePerSecond, long windowSizeMillis) {
            this.type = type;
            this.capacity = capacity;
            this.ratePerSecond = ratePerSecond;
            this.windowSizeMillis = windowSizeMillis;
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
    }
}
