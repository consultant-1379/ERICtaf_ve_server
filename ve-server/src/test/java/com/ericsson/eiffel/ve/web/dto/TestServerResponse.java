package com.ericsson.eiffel.ve.web.dto;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;

public class TestServerResponse {

	private ServerResponse unitUnderTest;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		unitUnderTest = new ServerResponse();
	}

	@Test
	public void testSetGetSender() {
		String sender = "Sender";
		unitUnderTest.setSender(sender);
		assertEquals(sender, unitUnderTest.getSender());
	}

	@Test
	public void testSetGetMessage() {
		String message = "Message";
		unitUnderTest.setMessage(message);
		assertEquals(message, unitUnderTest.getMessage());
	}

	@Test
	public void testSetGetServerData() {
		Map<String, Object> serverData = new HashMap<String, Object>();
		unitUnderTest.setServerData(serverData);
		assertEquals(serverData, unitUnderTest.getServerData());
	}

}
