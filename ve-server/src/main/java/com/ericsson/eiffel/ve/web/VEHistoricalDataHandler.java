package com.ericsson.eiffel.ve.web;

import java.io.IOException;
import java.util.Set;

import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.plugins.VEMessageHandler;
import com.ericsson.eiffel.ve.plugins.er.EventRepositoryHandler;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.web.dto.RESTEventImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VEHistoricalDataHandler implements AtmosphereHandler {
	
	private static final EiffelLog logger = new JavaLoggerEiffelLog(VEHistoricalDataHandler.class);
	private AtmosphereResponse res;
	private AtmosphereRequest req;
	private String method;
	private String uri;
	private JsonObject restEvent;
	private JsonObject eventBody;
	private JsonObject queryOptions;
	private JsonObject data;
	private final JsonParser parser = new JsonParser();

	@Override
	public void onRequest(AtmosphereResource resource) throws IOException {
		
		req = resource.getRequest();
		res = resource.getResponse();
		method = req.getMethod();
		uri = req.getRequestURI();
		
		restEvent = new JsonObject();
		eventBody = new JsonObject();
		
		logger.debug("onRequest. Method: " + method +" URI: " + uri);
		
		if ("POST".equalsIgnoreCase(method)) {
			data = parser.parse(req.getReader().readLine().trim()).getAsJsonObject();
			logger.debug("Received data: " + data.toString());
			
			if (data.has("model") && data.has("modelVersion") && data.has("query")) {
			
				if (data.has("queryOptions")) {
					queryOptions = data.getAsJsonObject("queryOptions");
				} else {
					queryOptions = new JsonObject();
				}
	
				eventBody.addProperty("model", data.get("model").getAsString()+"Model");
				eventBody.add("modelVersion", data.get("modelVersion"));
				eventBody.add("query", data.get("query"));
				eventBody.add("queryOptions", queryOptions);
				
				restEvent.addProperty("method", method);
				restEvent.addProperty("eventURI", "ve:"+uri);
				restEvent.addProperty("version", data.get("modelVersion").getAsString());
				restEvent.add("eventBody", eventBody);
	
				logger.debug("Created RESTEvent: " + restEvent.toString());
				logger.debug("Asking actor ...");
				
				String response = askActor(new RESTEventImpl(restEvent.toString()));
				logger.debug("Actor responded with: " + response);
				
				answerRequest(200, "OK", "application/json", response);
			} else {
				answerRequest(400, "Bad Request", "text/html", "");
			}
		} else {
			answerRequest(405, "Method not allowed", "text/html", "");
		}
	}

	@Override
	public void onStateChange(AtmosphereResourceEvent event) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
	
	private String askActor(RESTEvent restEvent) {
		Set<VEMessageHandler> messageHandlers = Bootstrap.getInstance().getMessageHandlers("ve:historicaldata/queryhandler");
		VEMessageHandler handler = null;
		for ( VEMessageHandler messageHandler : messageHandlers ) {
			if ( messageHandler instanceof EventRepositoryHandler ) {
				handler = messageHandler;
				break;
			}
		}
		
		RESTEvent event = null;
		if ( messageHandlers.size() <= 0 ) {
			logger.error("No messagehandler of type EventRepositoryHandler found.");
		} else {
			if ( messageHandlers.size() > 1 ) {
				logger.debug("Multiple messagehandlers of type EventRepositoryHandler found.");
			}
			
			try {
				event = handler.handleWithResponse(restEvent);
			} catch ( Exception e ) {
				answerRequest(500, "Internal server error", "text/html", "");
			}
		}
		
		return event.toString();
	}

	private void answerRequest(int status, String statusMessage, String contentType, String data) {
		res.setStatus(status, statusMessage);
		res.setContentType(contentType);
		res.write(data);
		try {
			res.flushBuffer();
		} catch (IOException e) {
			logger.error("Could not write REST response); " + e.getMessage());
		}
	}	
}
