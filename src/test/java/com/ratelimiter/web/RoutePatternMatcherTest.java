package com.ratelimiter.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutePatternMatcherTest {

    @Test
    void matchesExactPath() {
        assertTrue(RoutePatternMatcher.matches("/api/login", "/api/login"));
        assertFalse(RoutePatternMatcher.matches("/api/login", "/api/logout"));
    }

    @Test
    void singleStarMatchesOneSegment() {
        assertTrue(RoutePatternMatcher.matches("/api/*/ping", "/api/demo/ping"));
        assertFalse(RoutePatternMatcher.matches("/api/*/ping", "/api/demo/nested/ping"));
    }

    @Test
    void doubleStarMatchesAcrossSegments() {
        assertTrue(RoutePatternMatcher.matches("/api/export/**", "/api/export/data"));
        assertTrue(RoutePatternMatcher.matches("/api/export/**", "/api/export/data/nested"));
        assertFalse(RoutePatternMatcher.matches("/api/export/**", "/api/other/data"));
    }
}
