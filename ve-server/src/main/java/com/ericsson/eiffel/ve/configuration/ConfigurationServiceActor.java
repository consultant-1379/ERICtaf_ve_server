package com.ericsson.eiffel.ve.configuration;

import java.lang.reflect.Type;
import java.util.Map;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.web.dto.RESTEventImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class ConfigurationServiceActor extends UntypedActor {
	private final MongoDataStore store;
	private static final EiffelLog logger = new JavaLoggerEiffelLog(MongoDataStore.class);

	public ConfigurationServiceActor(final String hostname) {
		logger.debug("Creating ConfigurationServiceActor");
		store = new MongoDataStore(hostname);
	}

	@Override
	public void preStart() throws Exception {
		store.start();
	}

	@Override
	public void onReceive(Object message) throws VEConfigurationException {
		logger.debug("Received message: " + message.toString());
		if (message instanceof RESTEventImpl) {
			handle((RESTEvent) message);
		} else {
			unhandled(message);
		}
	}

	private void handle(RESTEvent message){
		logger.debug("Handle RESTEvent");
		JsonObject obj = message.getEventBody();
		JsonElement resourceId = obj.get("id");
		String requestBody = obj.get("requestBody").getAsString();
		String options = obj.get("options").getAsString();		
		JsonObject eventBody = new JsonObject();
		JsonObject responseBody = new JsonObject();
		responseBody.addProperty("version", "");
		responseBody.addProperty("method", "");
		responseBody.addProperty("eventURI", "");
		
		Gson gSon = new GsonBuilder().create();
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		Map<String, String> opt = gSon.fromJson(options, type);
		if(message.getEventURI().equalsIgnoreCase("dashboards")){
			switch (message.getMethod()){
			case "POST" :
				eventBody.addProperty("createdDashboard", store.postDashboard(requestBody));
				responseBody.add("eventBody",eventBody);
				getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());
				break; 
			case "PUT" : 
				eventBody.addProperty("updatedDashboard", store.putDashboard(requestBody));
				responseBody.add("eventBody", eventBody);
				getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());;
				break;
			case "DELETE" : 
				eventBody.addProperty("requestBody", store.deleteDashboard(resourceId.getAsString()));
				responseBody.add("eventBody", eventBody);
				getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());
				break;
			case "GET" :
				if (resourceId == null) {
					eventBody.addProperty("requestBody", store.getDashboards(opt));
					responseBody.add("eventBody", eventBody);
					getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());
				} else {
					eventBody.addProperty("requestBody", store.getDashboard(resourceId.getAsString()));
					responseBody.add("eventBody", eventBody);
					getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());
				}
				break;
			}
		}else if(message.getEventURI().equalsIgnoreCase("views")){
			switch (message.getMethod()){
			case "POST" :
				eventBody.addProperty("createdView", store.postView(requestBody));
				responseBody.add("eventBody",eventBody);
				getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());
				break; 
			case "PUT" : 
				eventBody.addProperty("updatedView", store.putView(requestBody));
				responseBody.add("eventBody", eventBody);
				getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());
				break;
			case "DELETE" : 
				eventBody.addProperty("requestBody", store.deleteView(resourceId.getAsString()));
				responseBody.add("eventBody", eventBody);
				getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());
				break;
			case "GET" :
				if (resourceId == null) {
					eventBody.addProperty("requestBody", store.getViews(opt));
					responseBody.add("eventBody", eventBody);
					getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());
				} else {
					eventBody.addProperty("requestBody", store.getView(resourceId.getAsString()));
					responseBody.add("eventBody", eventBody);
					getSender().tell(new RESTEventImpl(gSon.toJson(responseBody)), ActorRef.noSender());
				}
				break;
			}
		}
	}
}
