package com.ratelimiter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratelimiter.config.RateLimiterSettings;
import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.registry.RateLimiterRegistry;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Single Jetty {@link Handler} for the whole app: resolves the policy for
 * the request path, checks the caller's rate limiter, then dispatches to
 * one of the demo endpoints.
 */
@Singleton
public class RateLimiterHandler extends Handler.Abstract {

    private final RateLimiterSettings settings;
    private final RateLimiterRegistry registry;
    private final ClientKeyResolver clientKeyResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public RateLimiterHandler(RateLimiterSettings settings,
                               RateLimiterRegistry registry,
                               ClientKeyResolver clientKeyResolver) {
        this.settings = settings;
        this.registry = registry;
        this.clientKeyResolver = clientKeyResolver;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        String path = request.getHttpURI().getPath();
        String method = request.getMethod();

        String policyName = resolvePolicy(path);
        String clientKey = clientKeyResolver.resolve(request);
        RateLimiter rateLimiter = registry.resolve(policyName, clientKey);

        if (!rateLimiter.tryAcquire()) {
            return writeJson(response, callback, 429, Map.of(
                    "error", "Too Many Requests",
                    "policy", policyName,
                    "message", "Rate limit exceeded, please try again later."
            ));
        }

        return route(method, path, response, callback);
    }

    private boolean route(String method, String path, Response response, Callback callback) throws Exception {
        if ("GET".equals(method) && "/api/demo/ping".equals(path)) {
            return writeJson(response, callback, 200, Map.of("status", "ok"));
        }
        if ("POST".equals(method) && "/api/login".equals(path)) {
            return writeJson(response, callback, 200, Map.of("status", "login accepted"));
        }
        if ("GET".equals(method) && "/api/export/data".equals(path)) {
            return writeJson(response, callback, 200, Map.of("status", "export started"));
        }
        return writeJson(response, callback, 404, Map.of("error", "Not Found"));
    }

    private String resolvePolicy(String path) {
        for (Map.Entry<String, String> entry : settings.getRoutes().entrySet()) {
            if (RoutePatternMatcher.matches(entry.getKey(), path)) {
                return entry.getValue();
            }
        }
        return settings.getDefaultPolicy();
    }

    private boolean writeJson(Response response, Callback callback, int status, Object body) throws Exception {
        byte[] bytes = objectMapper.writeValueAsBytes(body);
        response.setStatus(status);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json");
        response.write(true, ByteBuffer.wrap(bytes), callback);
        return true;
    }
}
