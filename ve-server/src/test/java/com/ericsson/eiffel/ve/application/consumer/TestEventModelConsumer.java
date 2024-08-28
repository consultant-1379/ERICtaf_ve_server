package com.ericsson.eiffel.ve.application.consumer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.api.internal.VEConnection;
import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.application.model.EventModel;
import com.ericsson.eiffel.ve.web.dto.RESTEventImpl;
import com.google.gson.JsonObject;

public class TestEventModelConsumer {

	private EventModel unitUnderTest;
	private VEConnection connection;
	private RESTEvent subscriptionEvent;
	private JsonObject eventBody;
	
	@Before
	public void setup() throws Exception {
		VETestSupport.setupLogging();
		connection = mock(VEConnection.class);
		subscriptionEvent = mock(RESTEventImpl.class);
		eventBody = generateEventBody();
		
		when(subscriptionEvent.getMethod()).thenReturn("PUT");
		when(subscriptionEvent.getEventURI()).thenReturn("ve:livedata/subscriptions/myid");
		when(subscriptionEvent.getEventBody()).thenReturn(eventBody);
		
		unitUnderTest = new EventModel();
	}
	
	private JsonObject generateEventBody() {
		JsonObject eventBody = new JsonObject();
		eventBody.addProperty("query", "!eventType");
		return eventBody;
	}

	@Test
	public void testMatches() {
		unitUnderTest.initSubscription(subscriptionEvent, connection);
		assertTrue(unitUnderTest.matches("{}"));
	}
	
	@Test
	public void testConsume() {
		unitUnderTest.initSubscription(subscriptionEvent, connection);
		unitUnderTest.consume("{}");
		ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		verify(connection).send(topicCaptor.capture(), messageCaptor.capture());
		assertTrue(topicCaptor.getValue().equals("update"));
		assertTrue(messageCaptor.getValue().contains("\"data\":{}"));
	}
}
