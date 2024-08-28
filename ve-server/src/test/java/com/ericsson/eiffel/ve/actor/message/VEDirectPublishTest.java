package com.ericsson.eiffel.ve.actor.message;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.actor.message.VEDirectPublish;

public class VEDirectPublishTest {

	private String sessionId;
	private String json;
	private VEDirectPublish dp;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		sessionId = "TestId";
		json = "{ \"json\": \"json\"}";
		
		dp = new VEDirectPublish(sessionId, json);
	}

	@Test
	public void testGetSessionId() {
		assertTrue("TestId".equals(dp.getSessionId()));
	}

	@Test
	public void testGetJson() {
		assertTrue("{ \"json\": \"json\"}".equals(dp.getJson()));
	}
}
