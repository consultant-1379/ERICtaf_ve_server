package com.ericsson.eiffel.ve.actor.message;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.actor.message.VEPublish;

public class VEPublishTest {

	private String json;
	private VEPublish pub;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		json = "{ \"json\": \"json\"}";
		pub = new VEPublish(json);
	}

	@Test
	public void testGetJson() {
		assertTrue("{ \"json\": \"json\"}".equals(pub.getJson()));
	}
	
	@Test
	public void testGetSessionId() {
		assertNull(pub.getSessionId());
	}

}
