package com.ericsson.eiffel.ve.application;

import java.io.IOException;
import java.util.Date;

import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;

@AtmosphereHandlerService(path = "/chat",
interceptors = {AtmosphereResourceLifecycleInterceptor.class})
public class SocketIOChatAtmosphereHandler implements AtmosphereHandler {

    @Override
    public void onRequest(AtmosphereResource r) 
          throws IOException {
        r.getBroadcaster().broadcast(
            r.getRequest().getReader().readLine());
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        AtmosphereResource r = event.getResource();
        if (event.isSuspended()) {
            // THIS IS JUST FOR DEMO, use JACKSON instead.
            String body = event.getMessage().toString();
            r.write(("{ \"text\" : \"" 
                    + body + "\", \"time\" : " 
                    + new Date().getTime() + "}").getBytes());
        }
    }

    @Override
    public void destroy() {
    }
}