package com.ericsson.eiffel.ve.web;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.atmosphere.socketio.SocketIOException;
import org.atmosphere.socketio.SocketIOPacket;
import org.atmosphere.socketio.SocketIOSessionOutbound;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;

public class TestAtmosphereConnection {

	private AtmosphereConnection unitUnderTest;
	private SocketIOSessionOutbound session;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		session = mock(SocketIOSessionOutbound.class);
		unitUnderTest = new AtmosphereConnection(session);
	}

	@Test
	public void test() {
		try {
			unitUnderTest.send("topic", "message");
			verify(session).sendMessage(any(SocketIOPacket.class));
		} catch (SocketIOException e) {
			fail("Unexpected exception");
		}
	}

}
