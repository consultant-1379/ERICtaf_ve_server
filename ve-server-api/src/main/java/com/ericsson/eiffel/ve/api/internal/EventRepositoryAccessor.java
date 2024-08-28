package com.ericsson.eiffel.ve.api.internal;

/**
 * Interface used for classes providing access to an Event Repository. This gives
 * access to historical data.
 * @author olssodan
 *
 */
public interface EventRepositoryAccessor {

	/**
	 * The findEvent method retrieves the event with the given id from the Event
	 * Repository and returns it as a JSON String.
	 * @param id Id of the event to retrieve
	 * @return The event as a JSON String
	 */
	public String findEvent(String id);

	/**
	 * The findDownstream method retrieves all downstream events for the event
	 * with the given id from the Event Repository, and returns the result as a
	 * JSON Array in String format.
	 * @param id Event id to fetch downstream events for
	 * @return An array of events in JSON String format
	 */
	public String findDownstream(String id);

	/**
	 * The findUpstream method retrieves all upstream events for the event with
	 * the given id from the Event Repository, and returns the result as a JSON
	 * Array in String format.
	 * @param id Event id to fetch upstream events for
	 * @return An array of events in JSON String format
	 */
	public String findUpstream(String id);

	/**
	 * The findEvents method retrieves all Eiffel events from the Event Repository
	 * that matches the given query, and returns the result as a JSON Array in
	 * String format.
	 * @param query An ER query string to match events 
	 * @return An array of events in JSON String format
	 */
	public String findEvents(String query);

}
