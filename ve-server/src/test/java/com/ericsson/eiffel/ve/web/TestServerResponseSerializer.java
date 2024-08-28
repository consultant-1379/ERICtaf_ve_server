package com.ericsson.eiffel.ve.web;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobQueuedEvent;
import com.ericsson.duraci.eiffelmessage.messages.v2.EiffelMessageImpl;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestServerResponseSerializer {

	private ServerResponseSerializer unitUnderTest;
	
	private final String key = "name";
	private final String value = "data";
	
	private VEModel consumer;
	private Bootstrap bootstrap;
	private ObjectMapper objectMapper;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		consumer = mock(VEModel.class);
		objectMapper = mock(ObjectMapper.class);
		bootstrap = mock(Bootstrap.class);
		when(bootstrap.getObjectMapper()).thenReturn(objectMapper);
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");
	}

	@Test
	public void testData() {
		unitUnderTest = ServerResponseSerializer.create("sender");

		unitUnderTest.data(key, value);
		
		assertTrue(unitUnderTest.getServerData().containsKey(key));
		assertEquals(value, unitUnderTest.getServerData().get(key));
	}
	
	@Test
	public void testRegularMessage() {
		unitUnderTest = ServerResponseSerializer.create("sender", bootstrap);

		String message = "message";
		
		unitUnderTest.message(message);
		
		assertEquals(message, unitUnderTest.getResponse().getMessage());
		
		unitUnderTest.serialize(consumer);
		verify(consumer).consume(anyString());
	}
	
	@Test
	public void testEiffelMessage() {
		unitUnderTest = ServerResponseSerializer.create("sender", bootstrap);

		EiffelMessage eiffelMessage = new EiffelMessageImpl("domainId", EiffelJobQueuedEvent.Factory.create("job"));
		
		unitUnderTest.message(eiffelMessage);
		
		assertEquals(eiffelMessage, unitUnderTest.getResponse().getMessage());

		unitUnderTest.serialize(consumer);
		verify(consumer).consume(anyString());
	}

}
