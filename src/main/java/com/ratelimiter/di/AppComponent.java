package com.ratelimiter.di;

import com.ratelimiter.web.RateLimiterHandler;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = RateLimiterModule.class)
public interface AppComponent {

    RateLimiterHandler handler();
}
