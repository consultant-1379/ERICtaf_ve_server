package com.ericsson.eiffel.ve.web;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.socketio.SocketIOSessionOutbound;
import org.atmosphere.socketio.transport.DisconnectReason;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.plugins.VEMessageHandler;
import com.ericsson.eiffel.ve.web.dto.RESTEventImpl;
import com.google.gson.JsonObject;

public class TestVEDataHandler {

	private VEDataHandler unitUnderTest;
	private Bootstrap bootstrap;
	private VEService veService;
	private VEMessageHandler messageHandler;
	private Set<VEMessageHandler> messageHandlers;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		bootstrap = mock(Bootstrap.class);
		veService = mock(VEService.class);
		messageHandler = mock(VEMessageHandler.class);
		messageHandlers = new HashSet<VEMessageHandler>();
		messageHandlers.add(messageHandler);
		
		when(bootstrap.getVEService()).thenReturn(veService);
		when(bootstrap.getMessageHandlers(anyString())).thenReturn(messageHandlers);
		
		unitUnderTest = new VEDataHandler(bootstrap);
	}

	@Test
	public void testDestroy() {
		try {
			unitUnderTest.destroy();
		} catch(Exception e) {
			fail("Unexpected exception");
		}
	}

	@Test
	public void testOnConnect() throws IOException {
		AtmosphereResource event = mock(AtmosphereResource.class);
		SocketIOSessionOutbound handler = mock(SocketIOSessionOutbound.class);
		when(handler.getSessionId()).thenReturn("id");
		unitUnderTest.onConnect(event, handler);
		verify(veService).connect(matches("id"), any(AtmosphereConnection.class));
	}

	@Test
	public void testOnDisconnect() {
		AtmosphereResource event = mock(AtmosphereResource.class);
		SocketIOSessionOutbound handler = mock(SocketIOSessionOutbound.class);
		when(handler.getSessionId()).thenReturn("id");
		unitUnderTest.onDisconnect(event, handler, DisconnectReason.DISCONNECT);
		verify(veService).disconnect(matches("id"));
	}

	@Test
	public void testOnMessage() {
		AtmosphereResource event = mock(AtmosphereResource.class);
		SocketIOSessionOutbound handler = mock(SocketIOSessionOutbound.class);
		when(handler.getSessionId()).thenReturn("id");
		unitUnderTest.onMessage(event, handler, generateJsonMessage("eventURI"));
		verify(bootstrap).getMessageHandlers(matches("eventURI"));
		verify(messageHandler).handle(matches("id"), any(RESTEventImpl.class));
	}

	@Test
	public void testOnMessageWithNoEventURI() {
		AtmosphereResource event = mock(AtmosphereResource.class);
		SocketIOSessionOutbound handler = mock(SocketIOSessionOutbound.class);
		when(handler.getSessionId()).thenReturn("id");
		unitUnderTest.onMessage(event, handler, generateJsonMessage(null));
		verifyZeroInteractions(messageHandler);
	}

	private String generateJsonMessage(String eventURI) {
		JsonObject event = new JsonObject();
		event.addProperty("method", "method");
		if(eventURI != null)
			event.addProperty("eventURI", eventURI);
		event.addProperty("version", "version");
		event.add("eventBody", new JsonObject());
		return event.toString();
	}
}
