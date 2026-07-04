package com.ratelimiter.config;

import com.ratelimiter.core.RateLimiterType;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads the {@code rateLimiter} section of a classpath YAML file into a
 * {@link RateLimiterSettings}, without relying on a framework's binder.
 */
public final class YamlConfigLoader {

    private YamlConfigLoader() {
    }

    @SuppressWarnings("unchecked")
    public static RateLimiterSettings load(String classpathResource) {
        try (InputStream input = YamlConfigLoader.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (input == null) {
                throw new IllegalStateException("Config resource not found on classpath: " + classpathResource);
            }
            Map<String, Object> root = new Yaml().load(input);
            Map<String, Object> section = (Map<String, Object>) root.get("rateLimiter");
            if (section == null) {
                throw new IllegalStateException("Missing 'rateLimiter' section in " + classpathResource);
            }

            String defaultPolicy = (String) section.getOrDefault("defaultPolicy", "default");

            Map<String, RateLimiterSettings.PolicyConfig> policies = new LinkedHashMap<>();
            Map<String, Object> rawPolicies = (Map<String, Object>) section.getOrDefault("policies", Map.of());
            for (Map.Entry<String, Object> entry : rawPolicies.entrySet()) {
                policies.put(entry.getKey(), toPolicyConfig((Map<String, Object>) entry.getValue()));
            }

            Map<String, String> routes = new LinkedHashMap<>();
            Map<String, Object> rawRoutes = (Map<String, Object>) section.getOrDefault("routes", Map.of());
            for (Map.Entry<String, Object> entry : rawRoutes.entrySet()) {
                routes.put(entry.getKey(), String.valueOf(entry.getValue()));
            }

            return new RateLimiterSettings(defaultPolicy, policies, routes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + classpathResource, e);
        }
    }

    private static RateLimiterSettings.PolicyConfig toPolicyConfig(Map<String, Object> raw) {
        RateLimiterType type = RateLimiterType.valueOf((String) raw.get("type"));
        int capacity = ((Number) raw.getOrDefault("capacity", 10)).intValue();
        double ratePerSecond = ((Number) raw.getOrDefault("ratePerSecond", 1.0)).doubleValue();
        long windowSizeMillis = ((Number) raw.getOrDefault("windowSizeMillis", 1000)).longValue();
        return new RateLimiterSettings.PolicyConfig(type, capacity, ratePerSecond, windowSizeMillis);
    }
}
