package com.ericsson.eiffel.ve.actor.message;

public class VEPublish implements VEMessage {

    private final String json;

    public VEPublish(String json) {
        this.json = json;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    public String getJson() {
        return json;
    }
}
