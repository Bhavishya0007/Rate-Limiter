package com.ratelimiter.registry;

import com.ratelimiter.config.RateLimiterSettings;
import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.core.RateLimiterConfig;
import com.ratelimiter.core.factory.RateLimiterFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns one {@link RateLimiter} instance per (policy, client) pair so that
 * each caller is throttled independently under a shared policy definition.
 */
@Singleton
public class RateLimiterRegistry {

    private final RateLimiterSettings settings;
    private final Map<String, RateLimiter> instances = new ConcurrentHashMap<>();

    @Inject
    public RateLimiterRegistry(RateLimiterSettings settings) {
        this.settings = settings;
    }

    public RateLimiter resolve(String policyName, String clientKey) {
        RateLimiterSettings.PolicyConfig policyConfig = settings.getPolicies().get(policyName);
        if (policyConfig == null) {
            throw new IllegalArgumentException("Unknown rate limiter policy: " + policyName);
        }
        String cacheKey = policyName + ":" + clientKey;
        return instances.computeIfAbsent(cacheKey, key -> RateLimiterFactory.create(toConfig(policyConfig)));
    }

    private RateLimiterConfig toConfig(RateLimiterSettings.PolicyConfig policyConfig) {
        return RateLimiterConfig.builder(policyConfig.getType())
                .capacity(policyConfig.getCapacity())
                .ratePerSecond(policyConfig.getRatePerSecond())
                .windowSizeMillis(policyConfig.getWindowSizeMillis())
                .build();
    }
}
