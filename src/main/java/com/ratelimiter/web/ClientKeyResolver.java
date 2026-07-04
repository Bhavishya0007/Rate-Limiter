package com.ratelimiter.web;

import io.javalin.http.Context;

/**
 * Determines the identity a rate-limit policy is applied per (e.g. API key,
 * IP address, tenant id). Swap the implementation to change identity
 * strategy without touching the filter or the algorithms.
 */
public interface ClientKeyResolver {

    String resolve(Context ctx);
}
