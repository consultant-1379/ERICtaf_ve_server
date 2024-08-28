package com.ericsson.eiffel.ve.application.consumer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.api.internal.VEConnection;
import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.application.model.RatioDistributionModel;
import com.ericsson.eiffel.ve.web.dto.RESTEventImpl;
import com.google.gson.JsonObject;

public class TestRatioDistributionModelConsumer {

	private RatioDistributionModel unitUnderTest;
	private VEConnection connection;
	private RESTEvent subscriptionEvent;
	private JsonObject eventBody;
	private JsonObject queryOptions;
	
	@Before
	public void setup() throws Exception {
		VETestSupport.setupLogging();
		connection = mock(VEConnection.class);
		subscriptionEvent = mock(RESTEventImpl.class);
		eventBody = generateEventBody();

		when(subscriptionEvent.getMethod()).thenReturn("PUT");
		when(subscriptionEvent.getEventURI()).thenReturn("ve:livedata/subscriptions/myid");
		when(subscriptionEvent.getEventBody()).thenReturn(eventBody);

		unitUnderTest = new RatioDistributionModel();
	}

	private JsonObject generateEventBody() {
		JsonObject eventBody = new JsonObject();
		JsonObject queryOptions = new JsonObject();
		queryOptions.addProperty("base", "");
		queryOptions.addProperty("startTime", "");
		queryOptions.addProperty("endTime", "");
		eventBody.add("queryOptions", queryOptions);
		eventBody.addProperty("query", "eventType");
		eventBody.addProperty("updateInterval", 1);
		eventBody.addProperty("modelVersion", "1.0.0");
		return eventBody;
	}

	@Test
	public void testMatches() {
		unitUnderTest.initSubscription(subscriptionEvent, connection);
		assertTrue(unitUnderTest.matches("{\"eventType\" : \"EiffelJobStartedEvent\",\"eventTime\" : \"2014-03-18T13:17:32.191Z\"}"));
		unitUnderTest.delete();
	}
	
	@Test
	public void testConsume() {
		unitUnderTest.initSubscription(subscriptionEvent, connection);
		unitUnderTest.consume("{\"eventType\" : \"EiffelJobStartedEvent\"}");
		ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		verify(connection, timeout(1500)).send(topicCaptor.capture(), messageCaptor.capture());
		assertTrue(topicCaptor.getValue().equals("update"));
		assertTrue(messageCaptor.getValue().contains("\"label\":\"EiffelJobStartedEvent\",\"value\":1"));
		unitUnderTest.delete();
	}
}
