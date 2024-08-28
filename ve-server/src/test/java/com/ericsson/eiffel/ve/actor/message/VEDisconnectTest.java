package com.ericsson.eiffel.ve.actor.message;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.actor.message.VEDisconnect;

public class VEDisconnectTest {

	private final String sessionId = "TestId";
	private VEDisconnect disc;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		disc = new VEDisconnect(sessionId);
	}

	@Test
	public void testGetSessionId() {
		assertTrue("TestId".equals(disc.getSessionId()));
	}

}
