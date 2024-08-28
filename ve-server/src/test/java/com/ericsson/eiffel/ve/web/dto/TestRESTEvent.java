package com.ericsson.eiffel.ve.web.dto;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.VETestSupport;
import com.google.gson.JsonObject;

public class TestRESTEvent {
	
	private RESTEvent unitUnderTest;
	private final String method = "PUT";
	private final String eventURI = "uri";
	private final String version = "1.0";
	private final JsonObject eventBody = new JsonObject();
	private String json;

	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		eventBody.addProperty("property", "value");
		json = generateJson(method, eventURI, version, eventBody);
		unitUnderTest = new RESTEventImpl(json);
	}

	@Test
	public void testGetters() {
		assertEquals(method, unitUnderTest.getMethod());
		assertEquals(eventURI, unitUnderTest.getEventURI());
		assertEquals(version, unitUnderTest.getVersion());
		assertEquals(eventBody, unitUnderTest.getEventBody());
	}

	@Test
	public void testToString() {
		assertEquals(json, unitUnderTest.toString());
	}
	
	@Test
	public void testEquals() {
		RESTEvent equalsEvent = new RESTEventImpl(generateJson(method, eventURI, version, eventBody));
		RESTEvent differentMethod = new RESTEventImpl(generateJson("xxx", eventURI, version, eventBody));
		RESTEvent differentURI = new RESTEventImpl(generateJson(method, "xxx", version, eventBody));
		RESTEvent differentVersion = new RESTEventImpl(generateJson(method, eventURI, "x.x", eventBody));
		RESTEvent differentEventBody = new RESTEventImpl(generateJson(method, eventURI, version, new JsonObject()));
		
		assertTrue("Equals same event", unitUnderTest.equals(unitUnderTest));
		assertTrue("Equals identical event", unitUnderTest.equals(equalsEvent));
		assertFalse("Not equals null", unitUnderTest.equals(null));
		assertFalse("Not equals string", unitUnderTest.equals(unitUnderTest.toString()));
		assertFalse("Not equals different method", unitUnderTest.equals(differentMethod));
		assertFalse("Not equals different eventURI", unitUnderTest.equals(differentURI));
		assertFalse("Not equals different version", unitUnderTest.equals(differentVersion));
		assertFalse("Not equals different eventBody", unitUnderTest.equals(differentEventBody));
	}
	
	private String generateJson(String method, String eventURI, String version, JsonObject eventBody) {
		return "{\"method\":\""+method+"\",\"eventURI\":\""+eventURI+"\",\"version\":\""+version+"\",\"eventBody\":"+eventBody.toString()+"}";
	}
}
