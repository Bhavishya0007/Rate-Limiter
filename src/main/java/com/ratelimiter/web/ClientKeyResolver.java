package com.ratelimiter.web;

import org.eclipse.jetty.server.Request;

/**
 * Determines the identity a rate-limit policy is applied per (e.g. API key,
 * IP address, tenant id). Swap the implementation to change identity
 * strategy without touching the handler or the algorithms.
 */
public interface ClientKeyResolver {

    String resolve(Request request);
}
