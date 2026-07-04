package com.ratelimiter;

import com.ratelimiter.di.AppComponent;
import com.ratelimiter.di.DaggerAppComponent;
import org.eclipse.jetty.server.Server;

public final class RateLimiterApp {

    public static void main(String[] args) throws Exception {
        AppComponent component = DaggerAppComponent.create();

        Server server = new Server(8080);
        server.setHandler(component.handler());
        server.start();
        server.join();
    }
}
