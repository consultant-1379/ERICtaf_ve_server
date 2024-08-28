package com.ericsson.eiffel.ve.actor.message;

public class VEDirectPublish implements VEMessage {

    private final String sessionId;
    private final String json;

    public VEDirectPublish(String sessionId, String json) {
        this.sessionId = sessionId;
        this.json = json;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    public String getJson() {
        return json;
    }
}
