package com.ericsson.eiffel.ve.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;

import akka.actor.ActorRef;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.configuration.ConfigurationServiceHandler;
import com.ericsson.eiffel.ve.plugins.VEMessageHandler;
import com.ericsson.eiffel.ve.web.dto.RESTEventImpl;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class VEConfigurationServiceHandler implements AtmosphereHandler {
	private AtmosphereResponse res;
	private AtmosphereRequest req;
	private String method;
	private String uri;
	private String options;
	private String requestBody; 
	private static final int RESOURCE = 2;
	private static final EiffelLog logger = new JavaLoggerEiffelLog(VEConfigurationServiceHandler.class);
	
	public void onRequest(AtmosphereResource resource) throws IOException {

		req = resource.getRequest();
		res = resource.getResponse();
		method = req.getMethod();
		uri = req.getRequestURI();
    	requestBody = getRequestBody();
		String id = null;
		logger.debug("onRequest. " + "Method: "+method+" URI: "+uri);
		
		Gson gSon = new Gson();
		Map<String,String[]> paramMap = req.getParameterMap();
		Map<String, String> convertedParamMap = new HashMap<String, String>();
		for (Map.Entry<String, String[]> entry : paramMap.entrySet())
		{	
			String optArray = "";	
			optArray = Joiner.on(", ").join(entry.getValue());
			convertedParamMap.put(entry.getKey(), optArray);
		}
		options = gSon.toJson(convertedParamMap);
		
		String[] url = uri.split("/");
		if (url.length>4){				// /configuration/dashboards/<id>
			answerRequest(400, "Bad Request", "text/html", "");
			return;
		}else if (url.length == 4){
			id = url[3];
		}

		switch (method){
		case "GET" :
			switch (url[RESOURCE]){
			case "dashboards" :
				dashboardGetReq(id);
				break; 
			case "views" : 	
				viewGetReq(id);
				break;
			case "typeoptions" : 
				typeOptionsGetReq();
				break;
			default:
				answerRequest(400, "Bad Request", "text/html", "");
				break;
			}
			break;
		case "POST" : 	
			switch (url[RESOURCE]){
			case "dashboards" :
				dashboardPostReq();
				break; 
			case "views" : 	
				viewPostReq();
				break;
			default:
				answerRequest(400, "Bad Request", "text/html", "");
				break;
			}
			break;
		case "PUT" : 
			switch (url[RESOURCE]){
			case "dashboards" :
				dashboardPutReq(id);
				break; 
			case "views" : 	
				viewPutReq(id);
				break;
			default:
				answerRequest(400, "Bad Request", "text/html", "");
				break;
			}
			break;
		case "DELETE" : 
			switch (url[RESOURCE]){
			case "dashboards" :
				dashboardDeleteReq(id);
				break; 
			case "views" : 	
				viewDeleteReq(id);
				break;
			default:
				answerRequest(400, "Bad Request", "text/html", "");
				break;
			}
			break;
		default:
			answerRequest(400, "Bad Request", "text/html", "");
			break;
		}
	}
	
	private String getRequestBody() throws IOException {
		String body = "";
		InputStream is = req.getInputStream();
		int i;
		while((i=is.read())!=-1){
			body=body+(char)i;
		}
		return body;
	}
	
	private void answerRequest(int status, String statusMessage, String contentType, String data){
		res.setStatus(status, statusMessage);
		res.setContentType(contentType);
		res.write(data);
		try {
			res.flushBuffer();
		} catch (IOException e) {
			logger.error("Could not write REST response: " + e.getMessage());
		}
	}
	
	private RESTEvent askActor(String id, String request, String resource) {

		RESTEvent restEvent = createRESTEvent(id, request, resource);	
		Set<VEMessageHandler> messageHandlers = 
				Bootstrap.getInstance().getMessageHandlers("ve:rest/configurationhandler");
		VEMessageHandler handler = null;
		for (VEMessageHandler messageHandler : messageHandlers) {
			if (messageHandler instanceof ConfigurationServiceHandler){
				handler = messageHandler;  
				break;
			}
        } 
		
		RESTEvent event = null;
		if(messageHandlers.size()==0) {
			logger.debug("No messagehandler of type ConfigurationServiceHandler found");
		}else {
			if (messageHandlers.size() > 1) 
				logger.debug("Multiple messagehandlers of type ConfigurationServiceHandler found. ");
			try {
				event = handler.handleWithResponse(restEvent);
			} catch (Exception e) {
				answerRequest(500, "Internal server error", "html/text","");
			}			
		}
			
		return event;
	}
	
	
	private RESTEvent createRESTEvent(String id, String request, String resource) {

		JsonObject eventBody = new JsonObject();
		eventBody.addProperty("id", id);
		eventBody.addProperty("requestBody", requestBody);
		eventBody.addProperty("options", options);
		
		JsonObject restBody = new JsonObject();
		restBody.add("eventBody", eventBody);
		restBody.addProperty("version", "");
		restBody.addProperty("method", request);
		restBody.addProperty("eventURI", resource);
		Gson gSon = new GsonBuilder().create();
		
		return new RESTEventImpl(gSon.toJson(restBody));
	}
	 
	private void dashboardGetReq(String id){		
		RESTEvent result = askActor(id, "GET", "dashboards");
		if(result != null){
			if (result.getEventBody().has("requestBody")){
				answerRequest(200, "OK", "application/json", result.getEventBody()
						.get("requestBody").getAsString());	 
			}else{
				answerRequest(404, "Not found", "html/text", "");	
			}	 
		}
	}
	
	private void dashboardPostReq(){
		RESTEvent result = askActor(null, "POST", "dashboards");
		if(result != null){
			answerRequest(201, "Created", "html/text", result.getEventBody()
					.get("createdDashboard").getAsString());	 
		}
	}
	
	private void dashboardPutReq(String id){	
		RESTEvent result = askActor(null, "PUT", "dashboards");
		if(result != null){
			if (result.getEventBody().get("updatedDashboard").getAsString().isEmpty()){
				answerRequest(404, "Resource Not found", "html/text", "{}");	
			}else{
				answerRequest(200, "OK", "html/text", result.getEventBody().get("updatedDashboard").getAsString());	

			}	 
		}
	}

	private void dashboardDeleteReq(String id){	
		RESTEvent result = askActor(id, "DELETE", "dashboards");
		if(result != null){
			if (result.getEventBody().get("requestBody").getAsBoolean()){
				answerRequest(204, "No content", "html/text", "");	
			}else{
				answerRequest(404, "Resource Not found", "html/text", "");	
			}	 
		}
	}
	 
	private void viewGetReq(String id){
		RESTEvent result = askActor(id, "GET", "views");
		if(result != null){
			if (result.getEventBody().has("requestBody")){
				answerRequest(200, "OK", "application/json", result.getEventBody()
						.get("requestBody").getAsString());	 
			}else{
				answerRequest(404, "Resource Not found", "html/text", "");	
			}	 
		}
	}
	 
	private void viewPostReq(){
		RESTEvent result = askActor(null, "POST", "views");
		if(result != null){
			answerRequest(201, "Created", "html/text", result.getEventBody()
					.get("createdView").getAsString());	 
		}
	}
	
	private void viewPutReq(String id){
		RESTEvent result = askActor(null, "PUT", "views");
		if(result != null){
			if (result.getEventBody().get("updatedView").getAsString().isEmpty()){
				answerRequest(404, "Resource Not found", "html/text", "{}");
			}else{
				answerRequest(200, "OK", "html/text", result.getEventBody().get("updatedView").getAsString());	
			}	 
		}
	}
	
	private void viewDeleteReq(String id){
		RESTEvent result = askActor(id, "DELETE", "views");
		if(result != null){
			if (result.getEventBody().get("requestBody").getAsBoolean()){
				answerRequest(204, "No content", "html/text", "");	
			}else{
				answerRequest(404, "Resource Not found", "html/text", "");	
			}	 
		}
	}	
	
	private void typeOptionsGetReq(){
		answerRequest(501, "Not Implemented", "html/text","");
	}

	public void onStateChange(AtmosphereResourceEvent event) throws IOException {
	}

	public void destroy() {
	} 

}
