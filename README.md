# Rate-Limiter

A configurable rate limiter service in Java, built around the Strategy
pattern: four interchangeable algorithms behind one interface, selected per
route/client via config — no code changes needed to switch or tune a policy.

Wired with [Dagger](https://dagger.dev/) for dependency injection and served
over raw [Jetty](https://jetty.org/) (its core `Handler` API, no servlet API,
no Spring).

## Algorithms

All implement `com.ratelimiter.core.RateLimiter` (`tryAcquire()` /
`tryAcquire(int permits)`), thread-safe, selected by
`RateLimiterFactory` based on `RateLimiterType`:

| Type                   | Behavior                                                             |
|------------------------|-----------------------------------------------------------------------|
| `TOKEN_BUCKET`         | Allows bursts up to capacity, refills continuously at a fixed rate.  |
| `LEAKY_BUCKET`         | Smooths bursts to a constant outflow rate.                          |
| `FIXED_WINDOW_COUNTER` | Simplest: counts requests in a fixed window, resets on window roll. |
| `SLIDING_WINDOW_LOG`   | Precise: evicts individual request timestamps as the window slides. |

## Project layout

```
src/main/java/com/ratelimiter/
  core/                 RateLimiter interface, RateLimiterConfig, RateLimiterType
  core/algorithm/       the four strategy implementations
  core/factory/         RateLimiterFactory (config -> strategy instance)
  config/                RateLimiterSettings + YamlConfigLoader (reads application.yml)
  registry/              RateLimiterRegistry (one limiter instance per policy+client)
  web/                   RateLimiterHandler (Jetty Handler), ClientKeyResolver, RoutePatternMatcher
  di/                    Dagger module + component wiring it all together
  RateLimiterApp.java    entry point: builds the Dagger graph, starts a Jetty Server on :8080
src/main/resources/application.yml   named policies + route-to-policy mapping
```

## Configuring policies

Policies are named and defined in `application.yml`; routes map to a policy
by Ant-style pattern (`*` = one segment, `**` = any depth). Unmatched routes
fall back to `defaultPolicy`:

```yaml
rateLimiter:
  defaultPolicy: default
  policies:
    default:
      type: TOKEN_BUCKET
      capacity: 20
      ratePerSecond: 5
    strict:
      type: SLIDING_WINDOW_LOG
      capacity: 5
      windowSizeMillis: 1000
  routes:
    /api/demo/**: strict
```

Each client (resolved via the `X-API-Key` header, falling back to IP) gets
its own limiter instance per policy, so callers are throttled independently.

## Build & run

```bash
mvn test              # run the unit test suite
mvn exec:java          # run directly (dev loop), listens on :8080
mvn package -DskipTests && java -jar target/rate-limiter.jar   # run the packaged jar
```

## Try it

```bash
curl -i http://localhost:8080/api/demo/ping                       # strict: 5 req/sec before 429s
curl -i -X POST http://localhost:8080/api/login                   # login: 5 req/min
curl -i http://localhost:8080/api/export/data                     # export: leaky bucket, cap 10 / 2 per sec
curl -i http://localhost:8080/api/demo/ping -H "X-API-Key: alice"  # separate bucket per client key
```

A 429 response body identifies which policy rejected it:

```json
{"error":"Too Many Requests","policy":"strict","message":"Rate limit exceeded, please try again later."}
```

## Testing the algorithms

Unit tests use a `MutableClock` test double to advance time deterministically
instead of relying on `Thread.sleep`, so refill/leak/window-reset behavior is
verified exactly (see `src/test/java/com/ratelimiter/core/algorithm/`).
`RateLimiterFactoryTest` confirms each `RateLimiterType` resolves to the
correct strategy class, and `RoutePatternMatcherTest` covers the route
pattern matching used for policy selection.
