package com.ericsson.eiffel.ve.actor.message;

import com.ericsson.eiffel.ve.api.internal.VEConnection;

public class VEConnect implements VEMessage {

    private final String sessionId;
    private final VEConnection connection;

    public VEConnect(String sessionId, VEConnection connection) {
        this.sessionId = sessionId;
        this.connection = connection;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    public VEConnection getConnection() {
        return connection;
    }
}
