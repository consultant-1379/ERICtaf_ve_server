package com.ericsson.eiffel.ve.web.dto;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.VETestSupport;
import com.google.gson.JsonObject;

public class TestClientMessage {
	
	private ClientMessage unitUnderTest;
	private final String method = "PUT";
	private final String eventURI = "uri";
	private final String version = "1.0";
	private final String query = "test";
	private final String model = "model";
	private JsonObject eventBody;
	private String json;

	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		unitUnderTest = new ClientMessage();
		eventBody = new JsonObject();
		eventBody.addProperty("model", model);
		eventBody.addProperty("query", query);
		json = "{\"method\":\""+method+"\",\"eventURI\":\""+eventURI+"\",\"version\":\""+version+"\",\"eventBody\":"+eventBody.toString()+"}";
	}

	@Test
	public void testSetGetName() {
		String name = "name";
		unitUnderTest.setName(name);
		assertEquals(name, unitUnderTest.getName());
	}

	@Test
	public void testSetGetArgs() {
		List<Object> args = new ArrayList<Object>();
		args.add("Object1");
		unitUnderTest.setArgs(args);
		assertEquals(args, unitUnderTest.getArgs());
	}

	@Test
	public void testGetActualArgs() {
		List<Object> args = new ArrayList<Object>();
		args.add(json);
		unitUnderTest.setArgs(args);
		assertEquals(2, unitUnderTest.getActualArgs().size());
		assertEquals(query, unitUnderTest.getActualArgs().get("topic"));
		assertEquals(model, unitUnderTest.getActualArgs().get("model"));
	}

	@Test
	public void testGetEvent() {
		List<Object> args = new ArrayList<Object>();
		args.add(json);
		unitUnderTest.setArgs(args);
		RESTEvent event = new RESTEventImpl(json);
		assertEquals(event, unitUnderTest.getEvent());
	}

}
