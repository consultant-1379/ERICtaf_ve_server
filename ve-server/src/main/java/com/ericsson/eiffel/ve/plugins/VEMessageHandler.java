package com.ericsson.eiffel.ve.plugins;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;

public interface VEMessageHandler {
    String getResourceName();

    void handle(String sessionId, RESTEvent event);
    
    RESTEvent handleWithResponse(RESTEvent event) throws Exception;
    
}
