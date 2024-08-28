package com.ericsson.eiffel.ve.web.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.google.gson.JsonObject;

/**
 *
 */
public class ClientMessage {

    private String name;
    private List<Object> args;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object>  args) {
        this.args = args;
    }

    public Map<String,String> getActualArgs() {
    	Map<String,String> result = new HashMap<String,String>();
    	RESTEvent event = getEvent();
    	
    	if(event != null) {
    		JsonObject indata = event.getEventBody();
    		if(indata.has("query"))
    			result.put("topic", indata.get("query").getAsString());
    		if(indata.has("model"))
    			result.put("model", indata.get("model").getAsString());
    	}
    	
    	return result;
    }

    public RESTEvent getEvent() {
    	RESTEvent event = null;
    	
    	if(args.size() > 0)
    		event = new RESTEventImpl(args.get(0).toString());
    	
    	return event;
    }
}
