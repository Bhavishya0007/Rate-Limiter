package com.ratelimiter.web;

import java.util.regex.Pattern;

/**
 * Minimal Ant-style path matcher: {@code *} matches within a single path
 * segment, {@code **} matches across segments. Enough for route-to-policy
 * mapping without pulling in a full web framework's path matcher.
 */
public final class RoutePatternMatcher {

    private RoutePatternMatcher() {
    }

    public static boolean matches(String pattern, String path) {
        return path.matches(toRegex(pattern));
    }

    private static String toRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        int i = 0;
        while (i < pattern.length()) {
            char c = pattern.charAt(i);
            if (c == '*') {
                if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                    regex.append(".*");
                    i += 2;
                } else {
                    regex.append("[^/]*");
                    i += 1;
                }
            } else {
                regex.append(Pattern.quote(String.valueOf(c)));
                i += 1;
            }
        }
        return regex.toString();
    }
}
