package com.ericsson.eiffel.ve.actor.message;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;

public class VESubscribe implements VEMessage {

    private final String sessionId;
    private final RESTEvent event;

    public VESubscribe(String sessionId, RESTEvent event) {
        this.sessionId = sessionId;
        this.event = event;
    }

    public RESTEvent getEvent() {
    	return event;
    }
    
	@Override
    public String getSessionId() {
        return sessionId;
    }
}
