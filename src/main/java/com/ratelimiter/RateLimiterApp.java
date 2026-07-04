package com.ratelimiter;

import com.ratelimiter.config.RateLimiterSettings;
import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.di.AppComponent;
import com.ratelimiter.di.DaggerAppComponent;
import com.ratelimiter.registry.RateLimiterRegistry;
import com.ratelimiter.web.ClientKeyResolver;
import com.ratelimiter.web.RateLimitExceededException;
import com.ratelimiter.web.RoutePatternMatcher;
import io.javalin.Javalin;

import java.util.Map;

public final class RateLimiterApp {

    public static void main(String[] args) {
        AppComponent component = DaggerAppComponent.create();
        RateLimiterSettings settings = component.settings();
        RateLimiterRegistry registry = component.registry();
        ClientKeyResolver clientKeyResolver = component.clientKeyResolver();

        Javalin app = Javalin.create();

        app.before(ctx -> {
            String policyName = resolvePolicy(settings, ctx.path());
            String clientKey = clientKeyResolver.resolve(ctx);
            RateLimiter rateLimiter = registry.resolve(policyName, clientKey);
            if (!rateLimiter.tryAcquire()) {
                throw new RateLimitExceededException(policyName);
            }
        });

        app.exception(RateLimitExceededException.class, (e, ctx) -> ctx.status(429).json(Map.of(
                "error", "Too Many Requests",
                "policy", e.getPolicy(),
                "message", "Rate limit exceeded, please try again later."
        )));

        app.get("/api/demo/ping", ctx -> ctx.json(Map.of("status", "ok")));
        app.post("/api/login", ctx -> ctx.json(Map.of("status", "login accepted")));
        app.get("/api/export/data", ctx -> ctx.json(Map.of("status", "export started")));

        app.start(8080);
    }

    private static String resolvePolicy(RateLimiterSettings settings, String path) {
        for (Map.Entry<String, String> entry : settings.getRoutes().entrySet()) {
            if (RoutePatternMatcher.matches(entry.getKey(), path)) {
                return entry.getValue();
            }
        }
        return settings.getDefaultPolicy();
    }
}
