package com.ericsson.eiffel.ve.api.internal;

import com.google.gson.JsonObject;

/**
 * Interface for classes representing a RESTEvent in the VE server. The REST event
 * contains general information abouth the request, and the specific data that should
 * be used when handling the REST call.
 * 
 * TODO: Link to the Eiffel portal documentation of the REST services.
 * 
 * @author xdanols
 *
 */
public interface RESTEvent {

	/**
	 * The getMethod method returns which REST method the message was sent with.
	 * @return String, one of "GET", "POST", "PUT", "DELETE" or "ASYNC" 
	 */
	public String getMethod();

	/**
	 * The getEventURI method returns the URI that this event was sent to. Will contain
	 * eventual URI parameters.
	 * @return URI as a String
	 */
	public String getEventURI();

	/**
	 * The getVersion method returns which version that this event complies to. 
	 * @return Version as a String
	 */
	public String getVersion();

	/**
	 * The getEventBody return the body of the event as a JsonObject. The event body
	 * contains most parts of the data related to the actual request that this event
	 * represents.
	 * @return A JsonObject representing the event body
	 */
	public JsonObject getEventBody();

}