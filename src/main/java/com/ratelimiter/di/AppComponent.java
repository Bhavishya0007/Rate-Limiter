package com.ratelimiter.di;

import com.ratelimiter.config.RateLimiterSettings;
import com.ratelimiter.registry.RateLimiterRegistry;
import com.ratelimiter.web.ClientKeyResolver;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = RateLimiterModule.class)
public interface AppComponent {

    RateLimiterSettings settings();

    RateLimiterRegistry registry();

    ClientKeyResolver clientKeyResolver();
}
