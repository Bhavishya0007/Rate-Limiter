package com.ratelimiter.web;

import io.javalin.http.Context;

/**
 * Uses the {@code X-API-Key} header when present, falling back to the
 * caller's IP for anonymous requests.
 */
public class DefaultClientKeyResolver implements ClientKeyResolver {

    private static final String API_KEY_HEADER = "X-API-Key";

    @Override
    public String resolve(Context ctx) {
        String apiKey = ctx.header(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        return ctx.ip();
    }
}
