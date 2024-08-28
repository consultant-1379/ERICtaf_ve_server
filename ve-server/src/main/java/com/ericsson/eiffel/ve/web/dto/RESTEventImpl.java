package com.ericsson.eiffel.ve.web.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RESTEventImpl implements RESTEvent {
	private static final Logger logger = LoggerFactory.getLogger(RESTEventImpl.class.getName());
	
	private final String method;
	private final String eventURI;
	private final String version;
	private final JsonObject eventBody;
	
	public RESTEventImpl(String json) {
		logger.debug("Creating RESTEvent from: "+json);
		
		JsonObject event = parseJsonString(json);
		method = event.has("method") ? event.get("method").getAsString() : null;
		eventURI = event.has("eventURI") ? event.get("eventURI").getAsString() : null;
		version = event.has("version") ? event.get("version").getAsString() : null;
		eventBody = event.has("eventBody") ? event.getAsJsonObject("eventBody") : null;
	}
	
	/* (non-Javadoc)
	 * @see com.ericsson.eiffel.ve.web.dto.RESTEvent#getMethod()
	 */
	@Override
	public String getMethod() {
		return method;
	}

	/* (non-Javadoc)
	 * @see com.ericsson.eiffel.ve.web.dto.RESTEvent#getEventURI()
	 */
	@Override
	public String getEventURI() {
		return eventURI;
	}

	/* (non-Javadoc)
	 * @see com.ericsson.eiffel.ve.web.dto.RESTEvent#getVersion()
	 */
	@Override
	public String getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see com.ericsson.eiffel.ve.web.dto.RESTEvent#getEventBody()
	 */
	@Override
	public JsonObject getEventBody() {
		return eventBody;
	}
	
	private JsonObject parseJsonString(String json) {
		JsonParser parser = new JsonParser();
		JsonObject object = parser.parse(json).getAsJsonObject();
		
		if(object.has("args"))
			return parser.parse(object.getAsJsonArray("args").get(0).getAsString()).getAsJsonObject();
		
		return object;
	}
	
	@Override
	public String toString() {
		JsonObject event = new JsonObject();
		event.addProperty("method", method);
		event.addProperty("eventURI", eventURI);
		event.addProperty("version", version);
		event.add("eventBody", eventBody);
		return event.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(this == o)
			return true;
		if(!(o instanceof RESTEventImpl))
			return false;
		
		RESTEvent event = (RESTEvent)o;
		
		if(!method.equals(event.getMethod()))
			return false;
		if(!eventURI.equals(event.getEventURI()))
			return false;
		if(!version.equals(event.getVersion()))
			return false;
		if(!eventBody.equals(event.getEventBody()))
			return false;
				
		return true;
	}
}
