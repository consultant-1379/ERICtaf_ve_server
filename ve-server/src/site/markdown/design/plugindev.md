# Eiffel Visualization Engine (VE) Server

## Plugins

The Eiffel VE Server is extendable through an external Java API which makes it possible to add custom server based models that could be used from any client.

### API

The API for the VE server provides an interface that all models must implement, as well as some other interfaces used for data passing and help classes to handle data more easily.

* [**VEModel interface**](../../ve-server-api/apidocs/com/ericsson/eiffel/ve/api/VEModel.html)<br/>
This is the interface that all model plugins must implement to be recognized by the VE server.

* [**VEConnection interface**](../../ve-server-api/apidocs/com/ericsson/eiffel/ve/api/internal/VEConnection.html)<br/>
This interface represents a socket.io connection towards a client, and is used for sending updates from the server. No data is received through this interface.

* [**RESTEvent interface**](../../ve-server-api/apidocs/com/ericsson/eiffel/ve/api/internal/RESTEvent.html)<br/>
This interface represents a RESTEvent sent from the client. For a model, the RESTEvent will represent either a subscription request (input for `initSubscription` and `updateSubscription` in `VEModel`) or a historical data request (input for `query` in `VEModel`).

* [**EventRepositoryAccessor interface**](../../ve-server-api/apidocs/com/ericsson/eiffel/ve/api/internal/EventRepositoryAccessor.html)<br/>
This interface provides access to an Event Repository that could be used for fetching historical data.

* [**ResponseHandler help class**](../../ve-server-api/apidocs/com/ericsson/eiffel/ve/api/response/ResponseHandler.html)<br/>
This class provides convinience methods for generating response messages in the correct format that should be sent back to a VE client.

* [**QueryList help class**](../../ve-server-api/apidocs/com/ericsson/eiffel/ve/api/data/query/QueryList.html)<br/>
This class handles the VE Query Language at the server side. The `QueryList` class has a few help classes (`Query` and `QueryCondition`). These are normally only used internally in the `QueryList` class, so they are not listed separately here.

For a list of all classes in the VE server API, look [here](../../ve-server-api/apidocs).

### Implementing a plugin

To implement a plugin there are a few things to consider. This part will take up what dependencies are needed for a plugin project, 

#### Dependencies

To be able to develop a plugin for the VE server, the `ve-server-api` must be added as a dependency for the plugin project. This could either be downloaded as a JAR and linked into a local project or included as a dependency in a Maven project. The JAR is available for download from the [Eiffel Nexus](https://eiffel.lmera.ericsson.se/nexus/content/repositories/releases/com/ericsson/duraci/ve-server-api/). If using Maven, add the Eiffel repository (https://eiffel.lmera.ericsson.se/nexus/content/groups/public) to the project, and then add the dependency ve-server-api. It is recommended to build against the version of the API that matches the server version you are running against.

#### Requirements

The main requirement for a VE server plugin is that the main class of the plugin must implement the `VEModel` interface. Then there are some other things that also must be fulfilled by this class to fully work with the VE server:

* **Class must have a default constructor**<br/>
To make sure the server can instantiate the model implementation, there must be a default constructor available (meaning a constructor that doesn't require any arguments).
* **The class must be in a com.ericsson.\* java package**<br/>
It is required that the class is located in a com.ericsson.\* package, e.g. `com.ericsson.ve.my.plugin` or `com.ericsson.my.veserver.plugins`, to make sure that Reflections (library used for dynamically loading classes) can find the class.
* **The query method should not depend on subscription data**<br/>
The `query` method is not used for live subscription data, only for historical data from the Event Repository (or other source). When this method is called, it will be on an instance without a subscription, meaning that the `initSubscription` method hasn't been called.

#### Guidelines

Besides the requirements there are a few more recommendations that are good to follow when developing plugins:

* **Minimize the amount of data sent between server and client**<br/>
To avoid flooding of data towards the client and to keep up performance of the VE server it is recommended to use a timer mechanism in the model that sends out model data to the client at regular time intervals. The option `updateInterval` is usually included in the subscription event to define how often the client wants updates. Only send an update if something has happened to the data model.
* **Check that keys exist in the eventBody**<br/>
When reading data from the eventBody (which is a Google gson `JsonObject`), be sure to check if the key reading actually exists before trying to read it out. Since reading out a String from a JsonObject is made like `jsonObject.get("key").getAsString()`, it might cause NullPointerException if the key doesn't exist. Alternatively, read out `jsonObject.get("key")` first and check for null before calling `getAsString()`.
* **Subscription data not available in constructor**<br/>
Subscription data like matching query and other information are not available until `initSubscription` is called.

#### Example

This is an example of a plugin class for the VE server. It uses the `ScheduledThreadPoolExecutor` and implements the `Runnable` interface to handle the flooding via the `updateInterval` parameter. This is the recommended way. This plugin example does not use the Event Repository for any historical data queries (strictly works with live data).

	public class SimpleRatioDistributionModel implements VEModel, Runnable {
	
	    private static final EiffelLog logger = new JavaLoggerEiffelLog(SimpleRatioDistributionModel.class);
	
		private QueryList queryList;
		private RESTEvent subscriptionEvent = null;
		private VEConnection connection = null;
		private EventRepositoryAccessor eventRepositoryAccessor = null;
	
	    private final JsonObject jsonObject;
	    private boolean changed = false;
	    private ScheduledThreadPoolExecutor executor = null;
	    private String jsonReport;
	
		// No-argument constructor
		public SimpleRatioDistributionModel() {
	        this.jsonObject = generateRatioDistributionModelObject();
	    }
	
		// Initiates the subscription for this model instance, here it is recommended
		// to initiate instance variables that are dependent of subscription data, and to
		// setup eventual timer threads if updateInterval is being used
		@Override
		public void initSubscription(RESTEvent subscriptionEvent, VEConnection connection) {
			this.connection = connection;
			updateSubscription(subscriptionEvent);
	    	int updateInterval = subscriptionEvent.getEventBody().get("updateInterval").getAsInt();
	        executor = new ScheduledThreadPoolExecutor(1);
	        executor.scheduleWithFixedDelay(this, updateInterval, updateInterval, TimeUnit.SECONDS);
	    }
	
		// Updates the subscription this instance represents. Observe that the
		// updateInterval could have been changed here as well
		@Override
	    public void updateSubscription(RESTEvent subscriptionEvent) {
			this.subscriptionEvent = subscriptionEvent;
			this.queryList = QueryList.parseQueryString(this.subscriptionEvent.getEventBody().get("query").getAsString());
		}
	
	    // Consumes a message, only called if the matches method returned true
		// for the given message
		@Override
	    public void consume(String json) {
	    	logger.debug("Received message: " + json);
	    	final JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
	    	String eventType = jsonObj.get("eventType").getAsString();
	    	addEvent(jsonObject.getAsJsonArray("items"), eventType);
	    	jsonReport = ResponseHandler.generateUpdateMessageString(subscriptionEvent, jsonObject);
	    	changed = true;
	    }
	
		// This method is periodically executed by the ScheduledThreadPoolExecutor
		// Checks if there are changes in the model, and if there is sends an update
		// to the client
		@Override
		public void run() {
			if(changed) {
				logger.debug("Sending a status report");
				connection.send("update", jsonReport);
			}
			
			changed = false;
		}
	
		// In the delete method the executor thread is removed		
		@Override
		public void delete() {
			if(executor != null) {
				executor.shutdown();
				try {
					executor.awaitTermination(2, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
				}
			}
		}
		
		// Generate a JsonObject of the model to send to the client
		private JsonObject generateRatioDistributionModelObject() {
			JsonObject result = new JsonObject();
			result.add("modelMetaData", new JsonObject());
			result.getAsJsonObject("modelMetaData").addProperty("Type", "ratioDistribution");
			result.getAsJsonObject("modelMetaData").addProperty("Version", "1.0.0");
			result.add("items", new JsonArray());
			return result;
		}
		
		// Adds a received event to the model object (won't store the whole event)
		private void addEvent(JsonArray items, String eventType) {
			JsonObject item = null;
			for(int i = 0; i < items.size(); i++) {
				JsonObject temp = items.get(i).getAsJsonObject();
				if(eventType.equals(temp.get("label").getAsString()))
					item = temp;
			}
			if(item == null) {
				item = new JsonObject();
				item.addProperty("label", eventType);
				item.addProperty("value", 1);
				items.add(item);
			}
			else {
				item.addProperty("value", item.get("value").getAsInt() + 1);
			}
		}
	
		// Returns the model name this implementation represents
		// (remember that this should ALWAYS return same value independent of instance)
		@Override
		public String getModelName() {
			return getClass().getSimpleName();
		}
	
		// Usage of the QueryList to match an incoming message
		@Override
		public boolean matches(String json) {
			return queryList.matches(json);
		}
	
		// This method handles the historical data queries for this model implementation.
		// It is executed stand alone from the other methods that are part of a subscription.
		@Override
		public String query(RESTEvent queryEvent) {
			return null;
		}
	
		// Method for receiving an Event Repository accessor object. Observe that the accessor
		// object could be null here if no Event Repository is specified in the VE server
		// configuration
		@Override
		public void setEventRepositoryAccessor(
				EventRepositoryAccessor eventRepositoryAccessor) {
			this.eventRepositoryAccessor = eventRepositoryAccessor;
		}
	} 

### Using the model plugin

To use the plugin within the server, it needs to be built into a JAR file. This JAR can then be pointed out in the server configuration (see [Configuration](../configuration/index.html)), and the plugin handler in the VE server will automatically load all classes implementing the VEModel interface and make them available for usage in subscriptions and/or historical data requests. Observe that a JAR file may contain multiple models, it is not necessary to build a separate JAR for each model.