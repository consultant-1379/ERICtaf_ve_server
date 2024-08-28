package com.ericsson.eiffel.ve.actor.message;

public class VEDisconnect implements VEMessage {

    private final String sessionId;

    public VEDisconnect(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }
}
