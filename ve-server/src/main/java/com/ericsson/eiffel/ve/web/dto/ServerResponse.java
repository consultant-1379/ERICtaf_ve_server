package com.ericsson.eiffel.ve.web.dto;

import java.util.Map;

public class ServerResponse {

    private String sender;
    private Object message;
    private Map<String,Object> serverData;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Map<String,Object> getServerData() {
        return serverData;
    }

    public void setServerData(Map<String,Object> serverData) {
        this.serverData = serverData;
    }
}
