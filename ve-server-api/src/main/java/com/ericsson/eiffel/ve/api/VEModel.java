package com.ericsson.eiffel.ve.api;

import com.ericsson.eiffel.ve.api.internal.EventRepositoryAccessor;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.api.internal.VEConnection;

/**
 * This is the main extension point for adding new model implementations to the VE server.
 * Each class implementing this interface will be automatically identified as a model in
 * the server, and available for use from a VE client plugin.
 * 
 * Observe that a model can be used for two purposes, either subscribing to live data or
 * fetching historical data from an Event Repository.
 * 
 * TODO: Link to the Eiffel portal documentation of plugin development and REST services.
 * 
 * @author xdanols
 *
 */
public interface VEModel {
	/**
	 * The initSubscription method is called when this model is used for setting up a new
	 * live data subscription. This will only be called once for an instance of the
	 * implemented model. All new subscriptions will always start with a new model instance.
	 * If the subscription needs historical data to start with, it should be fetched here.
	 * @param subscriptionEvent A RESTEvent with all initial data needed for setting up the
	 * subscription.
	 * @param connection A VEConnection which is a socket.io connection towards the client,
	 * where model updates should be sent.
	 */
	public void initSubscription(RESTEvent subscriptionEvent, VEConnection connection);
	
	/**
	 * The updateSubscription method is called when the subscription this model instance is
	 * handling is being updated. It might be needed here to completely recalculate the stored
	 * model and then proceed as normal.
	 * @param subscriptionEvent A RESTEvent with the modified subscription data.
	 */
	public void updateSubscription(RESTEvent subscriptionEvent);
	
    /**
     * The delete method is called when a subscription is cancelled and before this instance
     * of the model is removed. Here it is a good practice to remove any threads or timers
     * that have been started within the model instance.
     */
	public void delete();
    
	/**
	 * The consume method is called when a message that is of interest for this model has
	 * been received (verified with the matches method). The input is a version neutral
	 * EiffelMessage in JSON String format.
	 * @param json JSON String representation of the received message
	 */
	public void consume(String json);
	
    /**
     * The getModelName method is called by the VE server when searching through available
     * model implementations to determine what name this model has. This method should always
     * return the same value independent of instance (consider it as a static method). The
     * model name returned here will be used to map against the model name that is specified
     * in subscription messages, to identify what model implementation that should be instanced
     * to serve the subscription.
     * @return A String containing the model name this class represents.
     */
	public String getModelName();
    
    /**
     * The query method is used for serving the historical data REST API of the VE server. This
     * method should only fetch historical data. When this method is called, there won't be any
     * subscription set up, or any client connection (VEConnection) available. The RESTEvent
     * contains the query and other parameters that should be used for fetching historical data.
     * @param queryEvent RESTEvent with initial data for the query.
     * @return The result of the query as a model in JSON String format.
     */
	public String query(RESTEvent queryEvent);
    
    /**
     * The matches method is called when a new message is received on the message bus to
     * determine whether or not the message is of interest to this model instance. The input
     * is a version neutral EiffelMessage in JSON String format.
     * @param json Received message in JSON format
     * @return True if the message should be used in this model, false otherwise
     */
	public boolean matches(String json);
    
    /**
     * The setEventRepositoryAccessor method is called from the server directly after an
     * instance of the class is created. It will provide an EventRepositoryAccessor instance
     * to the model implementation which can be used for fetching historical data.
     * NOTE: If no Event Repository is defined in the VE server configuration, this method will
     * be called with a null value, so if the given accessor is used, always check it for null.
     * @param eventRepositoryAccessor An EventRepositoryAccessor, if configured in the server
     * settings, null otherwise
     */
	public void setEventRepositoryAccessor(EventRepositoryAccessor eventRepositoryAccessor);
}
