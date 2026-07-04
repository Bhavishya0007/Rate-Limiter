package com.ratelimiter.web;

import org.eclipse.jetty.server.Request;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Uses the {@code X-API-Key} header when present, falling back to the
 * caller's remote address for anonymous requests.
 */
public class DefaultClientKeyResolver implements ClientKeyResolver {

    private static final String API_KEY_HEADER = "X-API-Key";

    @Override
    public String resolve(Request request) {
        String apiKey = request.getHeaders().get(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        SocketAddress remote = request.getConnectionMetaData().getRemoteSocketAddress();
        if (remote instanceof InetSocketAddress inetSocketAddress) {
            return inetSocketAddress.getAddress().getHostAddress();
        }
        return remote.toString();
    }
}
