package com.ericsson.eiffel.ve.application.consumer;
	
	import static org.junit.Assert.*;

	import java.util.logging.ConsoleHandler;
	import java.util.logging.Handler;
	import java.util.logging.Level;
	import java.util.logging.Logger;
	
	import org.junit.Before;
	import org.junit.Test;
	import org.mockito.ArgumentCaptor;
	
	import static org.mockito.Mockito.*;
	
	import com.ericsson.eiffel.ve.api.internal.VEConnection;
	import com.ericsson.eiffel.ve.application.model.DirectedAcyclicGraphModel;
	import com.ericsson.eiffel.ve.api.internal.RESTEvent;
	import com.google.gson.JsonObject;
	import com.google.gson.JsonParser;
	
	public class TestDirectedAcyclicGraphModelConsumer {
	
	    private DirectedAcyclicGraphModel unitUnderTest;
	    private VEConnection connection;
	    private RESTEvent subscriptionEvent1;
	    private JsonObject eventBody1;
	    private RESTEvent subscriptionEvent2;
	    private JsonObject eventBody2;
	    private RESTEvent subscriptionEvent3;
	    private JsonObject eventBody3;
	//    private RESTEvent query;
	//    private JsonObject queryBody;
	    
	    @Before
	    public void setup() {
	        connection = mock(VEConnection.class);
	        subscriptionEvent1 = mock(RESTEvent.class);
	        eventBody1 = generateEventBody1();
	        
	        when(subscriptionEvent1.getMethod()).thenReturn("PUT");
	        when(subscriptionEvent1.getEventURI()).thenReturn("ve:livedata/subscriptions/myid");
	        when(subscriptionEvent1.getEventBody()).thenReturn(eventBody1);
	        
	        subscriptionEvent2 = mock(RESTEvent.class);
	        eventBody2 = generateEventBody2();
	        
	        when(subscriptionEvent2.getMethod()).thenReturn("PUT");
	        when(subscriptionEvent2.getEventURI()).thenReturn("ve:livedata/subscriptions/myid");
	        when(subscriptionEvent2.getEventBody()).thenReturn(eventBody2);
	        
	        subscriptionEvent3 = mock(RESTEvent.class);
	        eventBody3 = generateEventBody3();
	        
	        when(subscriptionEvent3.getMethod()).thenReturn("PUT");
	        when(subscriptionEvent3.getEventURI()).thenReturn("ve:livedata/subscriptions/myid");
	        when(subscriptionEvent3.getEventBody()).thenReturn(eventBody3);
	        
	        unitUnderTest = new DirectedAcyclicGraphModel();
	        
	//        query = mock(RESTEvent.class);
	//        queryBody = generateQueryBody();
	//        
	//        when(query.getMethod()).thenReturn("PUT");
	//        when(query.getEventURI()).thenReturn("localhost:??/historicaldata/queryhandler");
	//        when(query.getEventBody()).thenReturn(queryBody);
	//        
	//        unitUnderTest = new DirectedAcyclicGraphModel();
	        
	        setLoggerLevel(Level.FINE);
	    }
	    
	    private JsonObject generateEventBody1() {
	
	        String jsonQuery = "{\"query\" : \"eventId=7dff4bb5-8264-478a-9374-764d21913931\", \"queryOptions\" : {\"type\" : \"flowChart\",\"base\" : \"eventId\",\"information\" : [\"domainId\",\"eventData.resultCode\"],\"includeConnections\" : true,\"dagAggregation\" : false,\"maxNumberOfDags\" : 10,\"pageNo\" : 1,\"pageSize\" : 20,\"title\" : \"eventType\",\"startDate\" : \"2014-02-26\", \"endDate\" : \"2014-02-28\"}, \"model\" : \"DirectedAcyclicGraphModel\", \"modelVersion\" : \"1.0\", \"updateinterval\" : 5}";
	        JsonObject eventBody1 = new JsonParser().parse(jsonQuery).getAsJsonObject();
	
	        return eventBody1;
	    }
	    
	    private JsonObject generateEventBody2() {
	
	        String jsonQuery = "{\"query\" : \"eventId=7dff4bb5-8264-478a-9374-764d21913931\", \"queryOptions\" : {\"type\" : \"flowChart\",\"base\" : \"eventType\",\"information\" : [\"domainId\",\"eventData.resultCode\"],\"includeConnections\" : true,\"dagAggregation\" : false,\"maxNumberOfDags\" : 10,\"pageNo\" : 1,\"pageSize\" : 20,\"title\" : \"eventType\",\"startDate\" : \"2014-02-26\", \"endDate\" : \"2014-02-28\"}, \"model\" : \"DirectedAcyclicGraphModel\", \"modelVersion\" : \"1.0\", \"updateInterval\" : 5}";
	        JsonObject eventBody2 = new JsonParser().parse(jsonQuery).getAsJsonObject();
	
	        return eventBody2;
	    }
	    
	    private JsonObject generateEventBody3() {
	        String jsonQuery = "{\"query\" : \"eventData.confidenceLevels.COMPILED=SUCCESS\", \"queryOptions\" : {\"type\" : \"flowChart\",\"base\" : \"eventId\",\"title\" : \"eventType\",\"information\" : [\"domainId\",\"eventData.confidenceLevels.COMPILED\"],\"includeConnections\" : true,\"dagAggregation\" : false,\"maxNumberOfDags\" : 10,\"pageNo\" : 1,\"pageSize\" : \"all\",\"sortField\" : \"eventTime\",\"sortOrder\" : \"descending\",\"startDate\" : \"2014-02-26\", \"endDate\" : \"2014-02-28\"}, \"model\" : \"DirectedAcyclicGraphModel\", \"modelVersion\" : \"1.0\", \"updateInterval\" : 5}";
	        JsonObject eventBody3 = new JsonParser().parse(jsonQuery).getAsJsonObject();
	
	        return eventBody3;
	    }
	    
	    private JsonObject generateEventBody4() {
	
	        String jsonQuery = "{\"query\" : \"eventId=7dff4bb5-8264-478a-9374-764d21913931\", \"queryOptions\" : {\"type\" : \"clusterChart\",\"base\" : \"eventType\",\"information\" : [\"domainId\",\"eventData.resultCode\"],\"includeConnections\" : true,\"dagAggregation\" : false,\"maxNumberOfDags\" : 10,\"pageNo\" : 1,\"pageSize\" : 20,\"title\" : \"eventType\",\"startDate\" : \"2014-02-26\", \"endDate\" : \"2014-02-28\"}, \"model\" : \"DirectedAcyclicGraphModel\", \"modelVersion\" : \"1.0\", \"updateInterval\" : 5}";
	        JsonObject eventBody4 = new JsonParser().parse(jsonQuery).getAsJsonObject();
	
	        return eventBody4;
	    }
	    
	    private JsonObject generateQueryBody() {
	
	        String jsonQuery = "{\"query\" : \"eventId=7dff4bb5-8264-478a-9374-764d21913931\", \"queryOptions\" : {\"base\" : \"JobExecutionId\",\"information\" : [\"domainId\",\"eventData.resultCode\"],\"includeConnections\" : true,\"dagAggregation\" : false,\"maxNumberOfDags\" : 10,\"pageNo\" : 1,\"pageSize\" : 20,\"title\" : \"eventType\",\"startDate\" : \"2014-02-26\", \"endDate\" : \"2014-02-28\"}, \"model\" : \"DirectedAcyclicGraphModel\", \"modelVersion\" : \"1.0\", \"updateInterval\" : 5}";
	        JsonObject jobBody = new JsonParser().parse(jsonQuery).getAsJsonObject();
	
	        return jobBody;
	    }
	
	    private void setLoggerLevel(Level level) {
	        //get the top Logger:
	        Logger topLogger = Logger.getLogger("");
	        //set the top logger level
	        topLogger.setLevel(level);
	
	        // Handler for console (reuse it if it already exists)
	        Handler consoleHandler = null;
	        //see if there is already a console handler
	        for (Handler handler : topLogger.getHandlers()) {
	            if (handler instanceof ConsoleHandler) {
	                //found the console handler
	                consoleHandler = handler;
	                break;
	            }
	        }
	
	        if (consoleHandler == null) {
	            //there was no console handler found, create a new one
	            consoleHandler = new ConsoleHandler();
	            topLogger.addHandler(consoleHandler);
	        }
	        //set the console handler level:
	        consoleHandler.setLevel(level);
	    }
	}
	    
	//    @Test
	//    public void testMatches() {
	//        unitUnderTest.initSubscription(subscriptionEvent1, connection);
	//        assertTrue(unitUnderTest.matches("{"
	//                    + "\"domainId\": \"VEDomain\","
	//                    + "\"eventId\": \"6cd3147f-3703-4747-8f2d-e3f13cdc2c41\","
	//                    + "\"eventTime\": \"2014-02-27T07:54:29.640Z\","
	//                    + "\"eventType\": \"EiffelJobFinishedEvent\","
	//                    + "\"inputEventIds\": ["
	//                        + "\"7dff4bb5-8264-478a-9374-764d21913931\""
	//                    + "],"
	//                    + "\"eventData\": {"
	//                        + "\"jobInstance\": \"create artifact\","
	//                        + "\"jobExecutionId\": \"bba43564-83b6-44f0-a6f6-f9dee31fdcc5\","
	//                        + "\"jobExecutionNumber\": 79,"
	//                        + "\"resultCode\": \"FAILURE\","
	//                        + "\"logReferences\": {},"
	//                        + "\"optionalParameters\": {}"
	//                    + "},"
	//                    + "\"eventSource\": {"
	//                        + "\"hostName\": \"WL2020601\","
	//                        + "\"pid\": 12264,"
	//                        + "\"name\": \"VE-Server\","
	//                        + "\"url\": \"http://127.0.0.1:8080/job/create%20artifact/79/\""
	//                    +"}"
	//                + "}"));
	//        unitUnderTest.delete();
	//    }
	    
	//    @Test
	//    public void testEventSubscription1() {
	//        // Subscribe on specific eventId and group on eventId, all events should be displayed and 
	//        // connected in connection
	//        unitUnderTest.initSubscription(subscriptionEvent1, connection);
	//        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
	//        verify(connection, timeout(1500)).send(captor.capture());
	//        assertTrue(captor.getValue().contains("\"uniqueId\":\"6cd3147f-3703-4747-8f2d-e3f13cdc2c41\",\"title\":\"EiffelJobFinishedEvent\""));
	//        unitUnderTest.delete();
	//    }
	//    
	//    @Test
	//    public void testEventSubscription2() {
	//        // Subscribe on specific eventId and group on eventType, the requested eventId eventType and 
	//        // eiffelJobFinischedEvent should be displayed and the rest eventIds will be stored in associatedEvents.
	//        unitUnderTest.initSubscription(subscriptionEvent2, connection);
	//        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
	//        verify(connection, timeout(1500)).send(captor.capture());
	//        assertTrue(captor.getValue().contains("\"uniqueId\":\"6cd3147f-3703-4747-8f2d-e3f13cdc2c41\",\"title\":\"EiffelJobFinishedEvent\""));
	//        unitUnderTest.delete();
	//    }
	//    
	//    @Test
	//    public void testEventSubscription3() {
	//        // Subscription on eventData.changeSet.contributor=superuser and group on eventId
	//        // Live data subscription Use Case
	//        unitUnderTest.initSubscription(subscriptionEvent3, connection);
	//        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
	//        verify(connection, timeout(1500)).send(captor.capture());
	//        assertTrue(captor.getValue().contains("\"eventData.confidenceLevels.COMPILED\":\"SUCCESS\""));
	//        unitUnderTest.delete();
	//    }
	    
	//    @Test
	//    public void testQuery() {
	//        unitUnderTest.query(query);
	//        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
	//        verify(connection, timeout(1500)).send(captor.capture());
	//        assertTrue(captor.getValue().contains("\"uniqueId\":\"6cd3147f-3703-4747-8f2d-e3f13cdc2c41\",\"title\":\"EiffelJobFinishedEvent\""));
	//        unitUnderTest.delete();
	//    }
