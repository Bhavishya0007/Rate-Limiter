package com.ratelimiter.di;

import com.ratelimiter.config.RateLimiterSettings;
import com.ratelimiter.config.YamlConfigLoader;
import com.ratelimiter.web.ClientKeyResolver;
import com.ratelimiter.web.DefaultClientKeyResolver;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class RateLimiterModule {

    @Provides
    @Singleton
    RateLimiterSettings provideSettings() {
        return YamlConfigLoader.load("application.yml");
    }

    @Provides
    @Singleton
    ClientKeyResolver provideClientKeyResolver() {
        return new DefaultClientKeyResolver();
    }
}
