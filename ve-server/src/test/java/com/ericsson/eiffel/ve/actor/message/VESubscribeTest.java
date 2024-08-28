package com.ericsson.eiffel.ve.actor.message;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.actor.message.VESubscribe;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;

public class VESubscribeTest {

	private String sessionId;
	private RESTEvent event;
	private VESubscribe subs;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
	}

	@Test
	public void test() {
//		fail("Not yet implemented");
	}

}
