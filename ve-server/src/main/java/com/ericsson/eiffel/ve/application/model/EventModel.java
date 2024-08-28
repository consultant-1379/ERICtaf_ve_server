package com.ericsson.eiffel.ve.application.model;



import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.api.data.query.QueryList;
import com.ericsson.eiffel.ve.api.internal.EventRepositoryAccessor;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.api.internal.VEConnection;
import com.ericsson.eiffel.ve.api.response.ResponseHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EventModel implements VEModel {
    private static final EiffelLog logger = new JavaLoggerEiffelLog(EventModel.class);

	private QueryList queryList;
	private RESTEvent subscriptionEvent = null;
	private VEConnection connection = null;

	public EventModel() {
	}

	@Override
	public void initSubscription(RESTEvent subscriptionEvent, VEConnection connection) {
		this.connection = connection;
		updateSubscription(subscriptionEvent);
	}

	@Override
	public void updateSubscription(RESTEvent subscriptionEvent) {
		this.subscriptionEvent = subscriptionEvent;
		this.queryList = QueryList.parseQueryString(this.subscriptionEvent.getEventBody().get("query").getAsString());
	}

	@Override
	public void consume(String json) {
		logger.debug("Received message:\n" + json);
		final JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
		String messageJson = generateMessageJson(jsonObj);
		connection.send("update", messageJson);
	}

	private String generateMessageJson(JsonObject asJsonObject) {
		JsonObject result = new JsonObject();
		result.add("modelMetaData", new JsonObject());
		result.getAsJsonObject("modelMetaData").addProperty("Type", "event");
		result.getAsJsonObject("modelMetaData").addProperty("Version", "1.0.0");
		result.add("data", asJsonObject);
		result.add("items", new JsonArray());
		return ResponseHandler.generateUpdateMessageString(subscriptionEvent, result);
	}

	@Override
	public void delete() {
		// Nothing needed to be done here
	}

	@Override
	public String getModelName() {
		return getClass().getSimpleName();
	}

	@Override
	public boolean matches(String json) {
		return queryList.matches(json);
	}

	@Override
	public String query(RESTEvent queryEvent) {
		return null;
	}

	@Override
	public void setEventRepositoryAccessor(
			EventRepositoryAccessor eventRepositoryAccessor) {
	}
}
