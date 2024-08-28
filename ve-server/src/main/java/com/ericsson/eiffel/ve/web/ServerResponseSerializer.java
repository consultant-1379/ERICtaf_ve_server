package com.ericsson.eiffel.ve.web;

import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.serialization.Serializer;
import com.ericsson.duraci.eiffelmessage.serialization.printing.exceptions.MessagePrintingException;
import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.web.dto.ServerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class ServerResponseSerializer {

	private final EiffelLog logger = new JavaLoggerEiffelLog(ServerResponseSerializer.class);

	private final Bootstrap bootstrap;
	
	private final ServerResponse response;
	private final Map<String, Object> serverData;

	private ServerResponseSerializer(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
		response = new ServerResponse();
		serverData = new HashMap<String, Object>();
		response.setServerData(serverData);
	}

	public static ServerResponseSerializer create(String sender) {
		return create(sender, Bootstrap.getInstance());
	}

	public static ServerResponseSerializer create(String sender, Bootstrap bootstrap) {
		ServerResponseSerializer serializer = new ServerResponseSerializer(bootstrap);
		serializer.response.setSender(sender);
		return serializer;
	}

	public ServerResponseSerializer message(Object message) {
		response.setMessage(message);
		return this;
	}

	public ServerResponseSerializer data(String name, Object data) {
		serverData.put(name, data);
		return this;
	}

	public void serialize(VEModel consumer) {
		if (response.getMessage() == null) {
			return;
		}
		ObjectMapper objectMapper = bootstrap.getObjectMapper();
		try {
			String json;

			// ObjectMapper cannot serialize EiffelMessage, needs to be handled separately
			if(response.getMessage() instanceof EiffelMessage) {
				json = serializeEiffelMessageResponse(response, objectMapper);
			} else {
				json = objectMapper.writeValueAsString(response);
			}
			consumer.consume(json);
		} catch (JsonProcessingException jpe) {
			logger.debug("Error processing JSON: "+jpe.getMessage());
		} catch (MessagePrintingException mpe) {
			logger.debug("Error serializing EiffelMessage: "+mpe.getMessage());
		}
	}

	private String serializeEiffelMessageResponse(ServerResponse response, ObjectMapper objectMapper) throws MessagePrintingException, JsonProcessingException {
		EiffelMessage message = (EiffelMessage)response.getMessage();
		JsonParser parser = new JsonParser();
		String serializedMessage = new Serializer(logger).compact(message).versionNeutralLatestOnly().print();
		JsonObject jsonMessage = parser.parse(serializedMessage).getAsJsonObject(); 

		response.setMessage("");
		String serializedResponse = objectMapper.writeValueAsString(response);

		JsonObject jsonResponse = parser.parse(serializedResponse).getAsJsonObject();
		jsonResponse.add("message", jsonMessage);

		return jsonResponse.toString();
	}

	public ServerResponse getResponse() {
		return response;
	}

	public Map<String, Object> getServerData() {
		return serverData;
	}
}
