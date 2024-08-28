package com.ericsson.eiffel.ve.actor.message;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.actor.message.VEConnect;
import com.ericsson.eiffel.ve.web.AtmosphereConnection;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.Mockito.mock;

public class VEConnectTest {

	private final String SessionId = "TestId";
	private AtmosphereConnection connection;
	private VEConnect connect;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		connection = mock(AtmosphereConnection.class);
		
		PowerMockito.whenNew(AtmosphereConnection.class).withAnyArguments().thenReturn(connection);

		connect = new VEConnect(SessionId, connection);
	}

	@Test
	public void testGetSessionId() {
		assertTrue("TestId".equals(connect.getSessionId()));
	}
	
	@Test
	public void testGetConnection() {
		assertTrue(connection.equals(connect.getConnection()));
	}
}
