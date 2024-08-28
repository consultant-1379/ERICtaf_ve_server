package com.ericsson.eiffel.ve.api.response;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.google.gson.JsonObject;

/**
 * Convenience class used to ease handling of responses from the server to the client.
 * Contains methods for generating messages with correct headers.
 * @author xdanols
 *
 */
public class ResponseHandler {
	
	/**
	 * The generateUpdateMessageString method generates a response message in the VE REST
	 * event format as a String. This event could then be sent to the client as model
	 * update. This method needs a RESTEvent this response is for (should be the event
	 * defining the subscription or query) and a JsonObject containing the model data.
	 * @param event A RESTEvent defining the subscription that the message updates
	 * @param eventBody A JsonObject containing model data that should be sent to the client
	 * @return A response message in JSON String format 
	 */
	public static String generateUpdateMessageString(RESTEvent event, JsonObject eventBody) {
		JsonObject reply = new JsonObject();
		
		reply.addProperty("eventURI", event.getEventURI());
		reply.addProperty("method", "ASYNC");
		reply.addProperty("version", event.getVersion());
		
		reply.add("eventBody", eventBody);
		
		return reply.toString();
	}
	
	/**
	 * The generateErrorMessageString method generates a response message in the VE REST 
	 * event format as a String. This event could then be sent to the client as an error 
	 * message. This method needs a RESTEvent that this response is for (should be the 
	 * event defining the subscription or query) and an error code and error message.  
	 * @param event A RESTEvent defining the subscription that was affected by the error
	 * @param code The error code.
	 * @param error The error message that describes the problem.
	 * @return A response message in JSON String format 
	 */
	public static String generateErrorMessageString(RESTEvent event, int code, String error) {
		JsonObject reply = new JsonObject();
		JsonObject eventBody = new JsonObject();
		
		eventBody.addProperty("code", code);
		eventBody.addProperty("error", error);
		
		reply.addProperty("eventURI", event.getEventURI());
		reply.addProperty("method", "ASYNC");
		reply.addProperty("version", event.getVersion());
		reply.add("eventBody", eventBody);
		
		return reply.toString();
	}
}
