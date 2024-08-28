package com.ericsson.eiffel.ve.api.data.query;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

public class QueryListTest {
	private String jsonMessage;
	
	@Before
	public void setup() {
		JsonObject root = new JsonObject();
		root.addProperty("eventType", "EiffelJobFinishedEvent");
		JsonObject data = new JsonObject();
		data.addProperty("jobExecutionId", 276);
		root.add("eventData", data);
		jsonMessage = root.toString();
	}

	@Test
	public void testSingleKeyExists() {
		QueryList result = QueryList.parseQueryString("eventType");
		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getConditions().size());
		assertEquals("eventType", result.get(0).getConditions().get(0).getKey());
		assertNull(result.get(0).getConditions().get(0).getValue());
		assertFalse(result.get(0).getConditions().get(0).isNegated());
		assertTrue(result.matches(jsonMessage));
	}

	@Test
	public void testSingleKeyShouldNotExist() {
		QueryList result = QueryList.parseQueryString("!eventType");
		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getConditions().size());
		assertEquals("eventType", result.get(0).getConditions().get(0).getKey());
		assertNull(result.get(0).getConditions().get(0).getValue());
		assertTrue(result.get(0).getConditions().get(0).isNegated());
		assertFalse(result.matches(jsonMessage));
	}
	
	@Test
	public void testSingleKeyHasValue() {
		QueryList result = QueryList.parseQueryString("eventType=EiffelJobFinishedEvent");
		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getConditions().size());
		assertEquals("eventType", result.get(0).getConditions().get(0).getKey());
		assertEquals("EiffelJobFinishedEvent", result.get(0).getConditions().get(0).getValue());
		assertFalse(result.get(0).getConditions().get(0).isNegated());
		assertTrue(result.matches(jsonMessage));
	}

	@Test
	public void testSingleNestedKey() {
		QueryList result = QueryList.parseQueryString("eventData.jobExecutionId=276");
		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getConditions().size());
		assertEquals("eventData.jobExecutionId", result.get(0).getConditions().get(0).getKey());
		assertEquals("276", result.get(0).getConditions().get(0).getValue());
		assertFalse(result.get(0).getConditions().get(0).isNegated());
		assertTrue(result.matches(jsonMessage));
	}
	
	@Test
	public void testAndRelationBetweenKeys() {
		QueryList result = QueryList.parseQueryString("eventType=EiffelJobFinishedEvent&&eventData.jobExecutionId=276");
		assertEquals(1, result.size());
		assertEquals(2, result.get(0).getConditions().size());
		assertEquals("eventType", result.get(0).getConditions().get(0).getKey());
		assertEquals("EiffelJobFinishedEvent", result.get(0).getConditions().get(0).getValue());
		assertEquals("eventData.jobExecutionId", result.get(0).getConditions().get(1).getKey());
		assertEquals("276", result.get(0).getConditions().get(1).getValue());
		assertTrue(result.matches(jsonMessage));
	}

	@Test
	public void testOrRelationBetweenKeys() {
		QueryList result = QueryList.parseQueryString("eventType=EiffelJobFinishedEvent||eventType=EiffelJobStartedEvent");
		assertEquals(2, result.size());
		assertEquals(1, result.get(0).getConditions().size());
		assertEquals(1, result.get(1).getConditions().size());
		assertEquals("eventType", result.get(0).getConditions().get(0).getKey());
		assertEquals("EiffelJobFinishedEvent", result.get(0).getConditions().get(0).getValue());
		assertEquals("eventType", result.get(1).getConditions().get(0).getKey());
		assertEquals("EiffelJobStartedEvent", result.get(1).getConditions().get(0).getValue());
		assertTrue(result.matches(jsonMessage));
	}

	@Test
	public void testMixedAndAndOrRelations() {
		QueryList result = QueryList.parseQueryString("eventType=EiffelJobFinishedEvent||eventType=EiffelJobStartedEvent&&eventData.jobExecutionId=276");
		assertEquals(2, result.size());
		assertEquals(1, result.get(0).getConditions().size());
		assertEquals(2, result.get(1).getConditions().size());
		assertEquals("eventType", result.get(0).getConditions().get(0).getKey());
		assertEquals("EiffelJobFinishedEvent", result.get(0).getConditions().get(0).getValue());
		assertEquals("eventType", result.get(1).getConditions().get(0).getKey());
		assertEquals("EiffelJobStartedEvent", result.get(1).getConditions().get(0).getValue());
		assertEquals("eventData.jobExecutionId", result.get(1).getConditions().get(1).getKey());
		assertEquals("276", result.get(1).getConditions().get(1).getValue());
		assertTrue(result.matches(jsonMessage));
	}

	@Test
	public void testNegatedExpression() {
		QueryList result = QueryList.parseQueryString("!eventType=EiffelJobFinishedEvent");
		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getConditions().size());
		assertEquals("eventType", result.get(0).getConditions().get(0).getKey());
		assertEquals("EiffelJobFinishedEvent", result.get(0).getConditions().get(0).getValue());
		assertTrue(result.get(0).getConditions().get(0).isNegated());
		assertFalse(result.matches(jsonMessage));
	}
	
	@Test
	public void testToString() {
		String query = "eventType=abc&&!data=hej||eventType=other";
		QueryList result = QueryList.parseQueryString(query);
		assertEquals(query, result.toString());
	}

	@Test
	public void testKeyNotInMessage() {
		QueryList result = QueryList.parseQueryString("otherType");
		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getConditions().size());
		assertEquals("otherType", result.get(0).getConditions().get(0).getKey());
		assertNull(result.get(0).getConditions().get(0).getValue());
		assertFalse(result.get(0).getConditions().get(0).isNegated());
		assertFalse(result.matches(jsonMessage));
	}
	
	@Test
	public void testERQueryList() {
		QueryList result = QueryList.parseQueryString("eventType=EiffelJobFinishedEvent||eventType=EiffelJobStartedEvent&&eventData.jobExecutionId=276");
		List<String> erQuery = result.getERQueryList();
		
		assertEquals("eventType=EiffelJobFinishedEvent", erQuery.get(0));
		assertEquals("eventType=EiffelJobStartedEvent&eventData.jobExecutionId=276", erQuery.get(1));
	}
}
