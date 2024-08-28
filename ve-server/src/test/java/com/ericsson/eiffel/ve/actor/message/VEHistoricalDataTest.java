package com.ericsson.eiffel.ve.actor.message;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.actor.message.VEHistoricalData;

public class VEHistoricalDataTest {

	private String sessionId;
	private String filter;
	private VEHistoricalData hd;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		sessionId = "TestId";
		filter = "{ \"json\": \"json\"}";
		
		hd = new VEHistoricalData(sessionId, filter);
	}

	@Test
	public void testGetSessionId() {
		assertTrue("TestId".equals(hd.getSessionId()));
	}

	@Test
	public void testGetFilter() {
		assertTrue("{ \"json\": \"json\"}".equals(hd.getFilter()));
	}
	
	@Test
	public void testToString() {
		assertTrue("{ \"json\": \"json\"}".equals(hd.toString()));
	}
}
