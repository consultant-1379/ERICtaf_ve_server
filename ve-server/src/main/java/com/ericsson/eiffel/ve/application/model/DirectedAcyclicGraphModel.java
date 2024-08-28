package com.ericsson.eiffel.ve.application.model;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.api.data.query.QueryList;
import com.ericsson.eiffel.ve.api.internal.EventRepositoryAccessor;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.api.internal.VEConnection;
import com.ericsson.eiffel.ve.api.response.ResponseHandler;
import com.google.gson.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DirectedAcyclicGraphModel implements VEModel, Runnable {

    private static final EiffelLog logger = new JavaLoggerEiffelLog(DirectedAcyclicGraphModel.class);
	private VEConnection connection;
	private RESTEvent subscriptionEvent = null;
	private String eventType;
	private final JsonObject jsonObject;
	private final HashMap<String,String> nodeRelations;
    private JsonObject queryOptions;
	private final LinkedHashMap<Integer,String> nodeTime;
	private boolean changed;
	private Integer maxId = -1;
	private EventRepositoryAccessor eventRepository = null;
    private String[] optionsToEr = {"startDate","endDate"};
	private String jsonReport;
	private QueryList queryList;
	private String type;

//	private final HashMap<Integer, String> responseCodes = new HashMap<Integer,String>() {
//		{
//			put(200, "OK");
//			put(400, "Bad Request");
//			put(404, "NotFound");
//			put(405, "Method not allowed");
//			put(500, "Internal Server Error");
//		}
//	};

	private ScheduledThreadPoolExecutor executor = null;
	
	public DirectedAcyclicGraphModel() {
		this.eventType = "event";
		this.jsonObject = generateDirectedAcyclicGraphModelObject();
		this.nodeRelations = new HashMap<String, String>();
		this.nodeTime = new LinkedHashMap<Integer, String>();
		this.changed = false;
	}
	
	@Override
    public String query(RESTEvent queryEvent) {
		
		this.queryOptions = queryEvent.getEventBody().get("queryOptions").getAsJsonObject();
		this.queryList = QueryList.parseQueryString(queryEvent.getEventBody().get("query").getAsString());
		this.type = "query";
		
    	if (getQueryOption("base").isEmpty()) {
    		logger.info("No base parameter value detected, using eventId as default");
    		queryOptions.addProperty("base", "eventId");
    	}
    	
    	String veType = getQueryOption("type");
    	if (veType.isEmpty())
    		veType = "flowChart";
    	
	    if ("flowChart".equals(veType)) {
	   		eventType = "event";
	   	}else if ("clusterChart".equals(veType)) {
	   		eventType = "job";
	   	}
		
	    manageItems(jsonObject);
    	
    	return ResponseHandler.generateUpdateMessageString(queryEvent, jsonObject);
    }
    
	@Override
    public void updateSubscription(RESTEvent subscriptionEvent) {
    	this.subscriptionEvent = subscriptionEvent;
    	this.type = "subscription";
    	
    	jsonObject.remove("items");
    	
    	initSubscription(this.subscriptionEvent, this.connection);
    }
    
    @Override
	public void initSubscription(RESTEvent subscriptionEvent, VEConnection connection) {
    	this.type = "subscription";
		this.connection = connection;
       	this.subscriptionEvent = subscriptionEvent;
       	JsonObject eventBody = subscriptionEvent.getEventBody();
       	this.queryOptions = eventBody.get("queryOptions").getAsJsonObject();
       	
       	String query = "";
    	
       	if (eventBody.has("query")) {
       		query = eventBody.get("query").getAsString();
       		if (query.split("=|<|>").length == 1) {
       			query = "!" + query + "=";
       		}
       		this.queryList = QueryList.parseQueryString(query);
    	
	    	if (getQueryOption("base").isEmpty()) {
	    		queryOptions.addProperty("base", "eventId");
	    	}
	    	
	    	String veType = getQueryOption("type");
	    	if (veType.isEmpty())
	    		veType = "flowChart";
	    	
		    if ("flowChart".equals(veType)) {
		    	eventType = "event";
		    }else if ("clusterChart".equals(veType)) {
		    	eventType = "job";
		    }
	    			
		    JsonObject queryResults = getJsonQueryResults(queryList);
			 
            logger.debug("QueryResults: " + queryResults.toString());
            
            for (int i = 0; i < queryResults.getAsJsonArray("items").size(); i++) {
                JsonObject event = queryResults.getAsJsonArray("items").get(i).getAsJsonObject();
                
                if (!checkRelation(event.get("eventId").getAsString())) // Check if the event already exists in subscription
                	manageItems(event);
            }
	        
			jsonReport = ResponseHandler.generateUpdateMessageString(subscriptionEvent, jsonObject);
			logger.debug("Report:" + jsonReport);
			
			if (jsonObject.getAsJsonArray("items").size() > 0)
				changed = true;
			
	    	Integer updateInterval = 5; // Default value for updateInterval
	    	if (eventBody.has("updateInterval")) {
	    		updateInterval = eventBody.get("updateInterval").getAsInt();
	    	}else{
	    		logger.info("No updateInterval parameter value detected, using 5 sec as default");
	    	}
	    	
			executor = new ScheduledThreadPoolExecutor(1);
	        executor.scheduleWithFixedDelay(this, 1, updateInterval, TimeUnit.SECONDS);
	        logger.debug("Update to client will be triggered every " + Integer.toString(updateInterval) + "seconds");
       	}else {
       		logger.info("No query parameter value detected, ending request");
       	}
	}
    
	@Override
	public void run() {
		if(changed) {
			logger.debug("Sending report!!");
			connection.send("update", jsonReport);
		}

		changed = false;
	}
	
	@Override
	public boolean matches(String json) {
		logger.debug("Received message(matches): " + json);
		JsonObject liveEvent = new JsonParser().parse(json).getAsJsonObject();
		String id = liveEvent.get("eventId").getAsString();
		JsonArray items = jsonObject.getAsJsonArray("items").getAsJsonArray();
		Boolean consume = false;
		
		if ("query".equals(type))
			return consume;
				
		for (int i = 0; i < items.size(); i++) {
			JsonArray dag = items.get(i).getAsJsonArray();
			JsonArray upstream = getUpstreamEventFromEr(id);
			if (upstream != null) {
				for (int j = 0; j < upstream.size(); j++) {
					JsonObject event = upstream.get(j).getAsJsonObject();
					String eventId = event.get("eventId").getAsString();
					if ((getLocalEventByUniqueId(dag, eventId) != null) && (isValidTime(event, getQueryOption("startDate"), getQueryOption("endDate")))) {
						consume = true;
						break;
					}
				}
			}
		}
		
		if (isValidTime(liveEvent, getQueryOption("startDate"), getQueryOption("endDate"))) {
			if (queryList.matches(json))
				consume = true;
		}
		
    	return consume;
    }
	
	@Override
	public void consume(String json) {
		final JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
		Boolean success = false;

		logger.debug("Receive message: " + json);
		String rootId = jsonObj.get("eventId").getAsString();
		
		if ("job".equals(eventType)) {
			logger.debug("Handling ClusterChart updates - Not implemented yet");
		}else if ("event".equals(eventType)) {
			logger.debug("Handling FlowChart updates");
			
			JsonArray upstream = getUpstreamEventFromEr(rootId);
			if ((upstream != null) && (jsonObject.getAsJsonArray("items").size() > 0)) {
				if (upstream.size() > 1) {
					for (int k = 1; k < upstream.size(); k++) {
						success = handleDagObjects(jsonObject.getAsJsonArray("items"), jsonObj, upstream.get(k).getAsJsonObject(), eventType);
					}
				}else {
					success = manageItems(jsonObj);
				}
			}else {
				success = manageItems(jsonObj);
			}
			if (!success)
				success = manageItems(jsonObj);
		}
		jsonReport = ResponseHandler.generateUpdateMessageString(subscriptionEvent, jsonObject);
	}
	
	@Override
	public void delete() {
		if(executor != null) {
			executor.shutdown();
			try {
				executor.awaitTermination(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public String getModelName() {
		return getClass().getSimpleName();
	}
	
	@Override
	public void setEventRepositoryAccessor(
			EventRepositoryAccessor eventRepositoryAccessor) {
			eventRepository = eventRepositoryAccessor;
	}

	private JsonObject generateDirectedAcyclicGraphModelObject() {
		JsonObject result = new JsonObject();
		result.add("modelMetaData", new JsonObject());
		result.getAsJsonObject("modelMetaData").addProperty("Type", "directedAcyclicGraphModel");
		result.getAsJsonObject("modelMetaData").addProperty("Version", "1.0");
		result.add("items", new JsonArray());
		//		{
		//			"modelMetaData" : {"Type" : "directedAcyclicGraphModel", "Version" : "1.2.3"},
		//			"items" : [
		//				[{	"id": 0,
		//					"uniqueId"  : "0c7d5153-7980-44e9-8542-b6292f22a1c8",
		//					"title"     : “EiffelBaselineDefinedEvent”,
		//					"information": {"domainId":"kista" "Status:",  "Unknown"},
		//					"status"    : "UNKNOWN",
		//					"connection": [{"id":2,"type":"inputEventId"},{"id":3,"type":"inputEventId"}],
		//					"associatedEvents"	: ["0c7d5153-7980-44e9-8542-b6292f22a1c1"]
		//				},            
		//				{	"id": 2,
		//					"uniqueId"  : "0c7d5153-7980-44e9-8542-b6292f22a1c0",
		//					"title"     : “EiffelBaselineDefinedEvent”,
		//					"information": {"domainId":"kista", "Status":"Success"},
		//					"status"    : "SUCCESS",
		//					"connections": [],
		//					"associatedEvents"	: []
		//		         }
		//			   ]
		//			  ]
		//			}
		return result;
	}

	// ========================== Node related ======================================================

	// Name:		getJsonQueryResults
	// Description:	Handle queries towards ER
	// Arguments:	queryList - List of queries to be sent to EventRewpository
	// Return:		JsonObject with results from all queries
	private JsonObject getJsonQueryResults(QueryList queryList) {
		logger.debug("Entering getJsonQueryResults");
		JsonArray totalItems = new JsonArray();
		JsonObject totalResults = new JsonObject();
		Integer totalNumberItems = 0;
		
		for (String query : queryList.getERQueryList()) {
			String erQuery = updateErQuery(query);
			logger.debug("erQuery:" + erQuery);
			JsonObject queryResults = new JsonParser().parse(eventRepository.findEvents(erQuery)).getAsJsonObject();
			totalNumberItems = totalNumberItems + queryResults.get("totalNumberItems").getAsInt();
			totalItems.addAll(queryResults.getAsJsonArray("items").getAsJsonArray());
		}
		totalResults.addProperty("totalNumberItems", totalNumberItems);
		totalResults.addProperty("pageNo", 1);
    	totalResults.addProperty("pageSize", totalNumberItems);
		totalResults.add("items", totalItems);
		return totalResults;
	}
	
	// Name:		updateErQuery
	// Description:	Updates query that will be sent to EventRepository with queryOption
	// Arguments:	erQuery - query string with original query
	// Return:		Updated erQuery with queryOptions added
	private String updateErQuery(String erQuery) {
		logger.debug("Entering updateErQuery");
		String append = "";
		for (int i = 0; i < optionsToEr.length; i++) {
	    	if (queryOptions.has(optionsToEr[i])) {
	    		if ("startDate".equals(optionsToEr[i])){
	    			append = append + "&eventTime>=" + queryOptions.get(optionsToEr[i]).getAsString();
	    		}else if ("endDate".equals(optionsToEr[i])){
	    			append = append + "&eventTime<=" + queryOptions.get(optionsToEr[i]).getAsString();
	    		}else {
	    			append = append + "&" + optionsToEr[i] + "=" + queryOptions.get(optionsToEr[i]).getAsString();
	    		}
	    	}
		}
		erQuery = erQuery + append;
		erQuery = erQuery.replace(">", "%3e");
		erQuery = erQuery.replace("<", "%3c");
		return erQuery;
	}

	// Name:		handleDagObject
	// Description:	Updates the dag objects and adds MB event nodes
	// Arguments:	items - the entire collection of dags
	//				upstreamEvent - Event to be searched for in the dag to confirm that it is part of this dag
	//				event - New event to be added if parent already in dag
	// Return:		true if newEvent added or exist in Dag
	//				false if newEvent do not exist in Dag
	private Boolean handleDagObjects(JsonArray items, JsonObject newEvent, JsonObject event, String type) {
		logger.debug("Entering handleDagObjects");
		JsonObject node = null;
		Boolean existsInDag = false;

		for (int i = 0; i < items.size(); i++) {
			JsonArray dag = items.get(i).getAsJsonArray();
			
			JsonObject parentNode = getLocalEventByUniqueId(dag, event.get("eventId").getAsString());
			
			if (parentNode != null) {
				existsInDag = true;
				if (checkDagSize(dag))
					continue; // Dag full no new event added
				
				if ("event".equals(type)) {
					node = addDownstreamEventNode(dag, event, newEvent, type);
				}else if ("job".equals(type)) {
					node = addDownstreamJobNode(dag, event, newEvent, type);
				}
				if (node != null) {
					dag.add(node);
					changed = true;
				}
			}
			if (dag.size() > 1)
				sortDagByEventTime(dag);
		}
		
		return existsInDag;
	}
	
	private Boolean manageItems(JsonObject jsonObj) {
		Boolean dagAggregation = false;
		if (queryOptions.has("dagAggregation"))
			dagAggregation = queryOptions.get("dagAggregation").getAsBoolean();
				
    	Integer maxNumberOfDags = 10;
    	if (!getQueryOption("maxNumberOfDags").isEmpty())
    		maxNumberOfDags = Integer.parseInt(getQueryOption("maxNumberOfDags"));
    	
       	if ((jsonObject.getAsJsonArray("items").size() <= maxNumberOfDags) || dagAggregation) {
			JsonArray dag = generateDagObject(jsonObj, eventType);
	       	if (dag.size() > 0)
	       		if (dagAggregation)
					if (jsonObject.getAsJsonArray("items").size() != 0) {
						jsonObject.getAsJsonArray("items").get(0).getAsJsonArray().addAll(dag);
						jsonReport = ResponseHandler.generateUpdateMessageString(subscriptionEvent, jsonObject);
						changed = true;
						return true;
					}
	       	jsonObject.getAsJsonArray("items").add(dag);
	       	changed = true;
	       	
       	}
       	return true;
	}

	// Name:		generateDagObject
	// Description:	Generates the initial dag objects and initiates adding of historical nodes
	// Arguments:	rootId - RootId from the result of the initial search query
	//				type - Type of subscription (flowChart: event clusterChart:job)
	// Return:		The created dag
	private JsonArray generateDagObject(JsonObject topEvent, String type) {
		JsonArray dag = new JsonArray();
		JsonArray streamEvents = null;
		JsonObject node = null;

		JsonObject event = topEvent;
		String rootId = event.get("eventId").getAsString();
		logger.debug("Entering generateDagObject with: rootId=" + rootId + " type=" + type);

		if ("job".equals(type)) {
			logger.info("Handling  ClusterChart subscriptions - not implemented yet");
			node = addUpstreamJobNode(dag, event, event, eventType);
			
		}else if ("event".equals(type)) {
			logger.info("Handling FlowChart subscriptions");
			node = addUpstreamEventNode(dag, event, event, eventType);
		}

		if (node != null)
			dag.add(node);
		
		streamEvents = getUpstreamEventFromEr(rootId);

		if (streamEvents != null) {
			logger.debug("upstreamEvents events found for rootId");
			for (int i = 1; i < streamEvents.size(); i++) {
				if (checkDagSize(dag)) {
					if (dag.size() > 1)
						sortDagByEventTime(dag);
					return dag;
				}
				JsonObject newEvent = streamEvents.get(i).getAsJsonObject();
				if ("event".equals(type)) {
					node = addUpstreamEventNode(dag, event, newEvent, eventType);
				}else if ("job".equals(type)) {
					node = addUpstreamJobNode(dag, event, newEvent, eventType);
				}
				if (node != null) {
					dag.add(node);
					event = newEvent;
				}
			}
		}
		event = topEvent;
		streamEvents = getDownstreamEventFromEr(rootId);
		
		if (streamEvents != null) {
			logger.debug("downstreamEvents events found for rootId");
			for (int i = 1; i < streamEvents.size(); i++) {
				if (checkDagSize(dag)) {
					if (dag.size() > 1)
						sortDagByEventTime(dag);
					return dag;
				}
				JsonObject newEvent = streamEvents.get(i).getAsJsonObject();
				if ("event".equals(type)) {
					node = addDownstreamEventNode(dag, event, newEvent, eventType); 
				}else if ("job".equals(type)) {
					node = addDownstreamJobNode(dag, event, newEvent, eventType);
				}
				if (node != null) {
					dag.add(node);
					event = newEvent;
				}
			}
		}
		if (dag.size() > 1)
			sortDagByEventTime(dag);
		return dag;
	}

	// Name:		addUpstreamJobNode
	// Description:	Creates a new node to be inserted in a dag( Directed Acyclic Graph).
	//				Initiates status updates if required.
	// Argument:	dag - Directed Acyclic Graph array
	//				rootEvent - The event that has newEvent as upstreamEvent
	//				newEvent -	The new Event, possibly a new node
	//				type -		eventType of this dag
	// Return:		New node to be insterted in dag
	private JsonObject addUpstreamJobNode(JsonArray dag, JsonObject rootEvent, JsonObject newEvent, String type) {
		logger.debug("Entering addUpstreamJobNode");
		JsonObject node = null;
		String newEventType = newEvent.get("eventType").getAsString();
		String newId = newEvent.get("eventData").getAsJsonObject().get("jobExecutionId").getAsString();
		String rootId = rootEvent.get("eventData").getAsJsonObject().get("jobExecutionId").getAsString();
		
		JsonObject rootNode = getLocalEventByUniqueId(dag, rootId);
		
		String status = "UNKNOWN";
		if (newEvent.get("eventData").getAsJsonObject().has("resultCode"))
			status = newEvent.get("eventData").getAsJsonObject().get("resultCode").getAsString();
		
		if (rootNode != null) {
			addRelation(rootId, newEvent.get("eventId").getAsString(), dag);
				if (("EiffelJobFinishedEvent".equals(newEventType)) ||
						("EiffelJobModifiedEvent".equals(newEventType))) {
					changeNodeStatus(rootNode, newEvent, status);
				}
			if ((!rootNode.get("title").getAsString().equals(newEventType)) || (getLocalEventByUniqueId(dag, newId) != null)) {
				return null;
			}
		}
		
		node = addNode(newEvent, status, type);
		addConnections(rootNode, maxId.toString(), "inputEventId");
		return node;
	}
	
	// Name:		addDownstreamJobNode
	// Description:	Creates a new node to be inserted in a dag( Directed Acyclic Graph).
	//				Initiates status updates if required.
	// Argument:	dag - Directed Acyclic Graph  array
	//				rootEvent - The event that has newEvent as downstreamEvent
	//				newEvent - The new Event, possibly a new node
	// Return:		New node to be insterted in dag
	private JsonObject addDownstreamJobNode(JsonArray dag, JsonObject rootEvent, JsonObject newEvent, String type) {
		logger.debug("Entering addDownstreamJobNode");
		JsonObject node = null;
		String rootId = rootEvent.get("eventData").getAsJsonObject().get("jobExecutionId").getAsString();
		String newId = newEvent.get("eventData").getAsJsonObject().get("jobExecutionId").getAsString();
		String newEventType = newEvent.get("eventType").getAsString();
		JsonObject rootNode = getLocalEventByUniqueId(dag, rootId);
		
		String status = "UNKNOWN";
		if (newEvent.get("eventData").getAsJsonObject().has("resultCode"))
			status = newEvent.get("eventData").getAsJsonObject().get("resultCode").getAsString();
		
		if (rootNode != null) {
			addRelation(rootId, newEvent.get("eventId").getAsString(), dag);
				if (("EiffelJobFinishedEvent".equals(newEventType)) ||
						("EiffelJobModifiedEvent".equals(newEventType))) {
					changeNodeStatus(rootNode, newEvent, status);
				}
				if (!(newEventType.equals(rootNode.get("title").getAsString())) || (getLocalEventByUniqueId(dag, newId) != null)) {
					return null;
				}
			node = addNode(newEvent, status, eventType);
			addConnections(node, rootNode.get("id").getAsString(), "inputEventId");
		}else {
			logger.debug("RootId:" + rootId + " does not exist!");
		}
		return node;
	}
	
	// Name:		addUpstreamEventNode
	// Description:	Creates a new node to be inserted in a dag( Directed Acyclic Graph).
	//				Initiates status updates if required.
	// Argument:	dag - Directed Acyclic Graph array
	//				rootEvent - The event that has newEvent as upstreamEvent
	//				newEvent -	The new Event, possibly a new node
	//				type -		eventType of this dag
	// Return:		New node to be insterted in dag
	private JsonObject addUpstreamEventNode(JsonArray dag, JsonObject rootEvent, JsonObject newEvent, String type) {
		logger.debug("Entering addUpstreamEventNode");
		JsonObject node = null;
		String rootId = rootEvent.get("eventId").getAsString();
		String newId = newEvent.get("eventId").getAsString();
		JsonObject rootNode = getLocalEventByUniqueId(dag, rootId);
		String newEventType = newEvent.get("eventType").getAsString();
		
		String status = "UNKNOWN";
		if (newEvent.get("eventData").getAsJsonObject().has("resultCode"))
			status = newEvent.get("eventData").getAsJsonObject().get("resultCode").getAsString();
			
		if (getLocalEventByUniqueId(dag, newId) != null) {
			logger.debug("A node for " + newId + " already exists!");
			return null;
		}
		
		if (rootNode != null) {
			if ("eventId".equals(getQueryOption("base"))) {
			}else if ("eventType".equals(getQueryOption("base"))) {
				if (newEvent.has("resultCode")) {
					if (("EiffelJobFinishedEvent".equals(newEventType)) ||
							("EiffelJobModifiedEvent".equals(newEventType))) {
						changeNodeStatus(rootNode, newEvent, status);
					}
				}
				if (!((rootNode.get("title").getAsString().equals(newEventType)) || 
						("EiffelJobFinishedEvent".equals(newEventType)) ||
						("EiffelJobModifiedEvent".equals(newEventType)))) {
					addRelation(rootId, newId, dag);
					return null;
				}else {
					addRelation(newId, newId, dag);
				}
			}
			Integer id = maxId + 1;
			addConnections(rootNode, id.toString(), "inputEventId");
		}
		node = addNode(newEvent, status, eventType);
		return node;
	}
		
	// Name:		addDownstreamEventNode
	// Description:	Creates a new node to be inserted in a dag( Directed Acyclic Graph).
	//				Initiates status updates if required.
	// Argument:	dag - Directed Acyclic Graph  array
	//				rootEvent - The event that has newEvent as downstreamEvent
	//				newEvent - The new Event, possibly a new node
	// Return:		New node to be insterted in dag
	private JsonObject addDownstreamEventNode(JsonArray dag, JsonObject rootEvent, JsonObject newEvent, String type) {
		logger.debug("Entering addDownstreamEventNode");
		JsonObject node = null;
		String rootId = rootEvent.get("eventId").getAsString();
		String newId = newEvent.get("eventId").getAsString();
		JsonObject rootNode = getLocalEventByUniqueId(dag, rootId);
		String newEventType = newEvent.get("eventType").getAsString();
		
		String status = "UNKNOWN";
		if (newEvent.get("eventData").getAsJsonObject().has("resultCode"))
			status = newEvent.get("eventData").getAsJsonObject().get("resultCode").getAsString();
		
		if (getLocalEventByUniqueId(dag, newId) != null) {
			logger.debug("A node for " + newId + " already exists!");
			return node;
		}

		if (rootNode != null) {
			if ("eventId".equals(getQueryOption("base"))) {
				node = addNode(newEvent, status, eventType);
				addConnections(node, rootNode.get("id").getAsString(), "inputEventId");
			}else if ("eventType".equals(getQueryOption("base"))) {
				if (("EiffelJobFinishedEvent".equals(newEventType)) ||
						("EiffelJobModifiedEvent".equals(newEventType))) {
					changeNodeStatus(rootNode, newEvent, status);
				}
				if ((rootNode.get("title").getAsString().equals(newEventType)) ||
						("EiffelJobFinishedEvent".equals(newEventType)) ||
						("EiffelJobModifiedEvent".equals(newEventType))) {
					node = addNode(newEvent, status, eventType);
					addConnections(node, rootNode.get("id").getAsString(), "inputEventId");
					if (node == null) {
						return node;
					}
				}
				addRelation(rootId, newId, dag);
			}
		}else {
			logger.debug("RootId:" + rootId + " does not exist!");
		}
		return node;
	}

	// Name:		addNode
	// Description:	Adding new nodes to a dag
	// Arguments:	newEvent - The new event to be added
	//				status - The status string to be added to the new node
	// Return:		The new node
	private JsonObject addNode(JsonObject newEvent, String status, String type) {
		JsonObject node = new JsonObject();
		JsonArray connections = new JsonArray();
		JsonArray associatedEvents;
		String newId;

		if ("event".equals(type)) {
			newId = newEvent.get("eventId").getAsString();
		}else if ("job".equals(type)) {
			newId = newEvent.get("eventData").getAsJsonObject().get("jobExecutionId").getAsString();
		}else {
			return null;
		}

		associatedEvents = getRelation(newId);

		maxId = maxId + 1;
		
		JsonObject information = handleOptions(newEvent, "information");
		if (information == null)
			information = new JsonObject();
		
		JsonObject title = handleOptions(newEvent, "title");

		node.addProperty("id", maxId);
		node.addProperty("type", type);
		node.addProperty("uniqueId", newId);
		if (title != null) {
			String titleKey = queryOptions.get("title").getAsString();
			node.addProperty("title", title.get(titleKey).getAsString());
		}else {
			node.addProperty("title", "");
		}
		node.add("information", information);
		node.addProperty("status", status);
		if (queryOptions.has("includeConnections"))
			if (queryOptions.get("includeConnections").getAsBoolean())
				node.add("connection", connections);
		
		node.add("associatedEvents", associatedEvents);

		nodeTime.put(maxId,newEvent.get("eventTime").getAsString());
		return node;
	}

	// Name:		checkDagSize
	// Description:	Checks if the Dag has reached the specified maximum number of nodes
	// Arguments:	dag - Directed Acyclic Graph  array
	// Return:		Boolean value that specifies if the max number of Nodes have been reached
	//					- true if the dag has reached the maximum number of Nodes
	//					- false if the dag has not reached the maximum number of Nodes
	private Boolean checkDagSize(JsonArray dag) {
		String pageSize = getQueryOption("pageSize");
		if (!"".equals(pageSize)) {
			if (!"all".equals(pageSize))
				if (dag.size() > Integer.parseInt(pageSize)) {
					logger.debug("This dag has reached the maximum (" + pageSize + ") number of nodes specified");
					return true;
				}
		}
		return false;
	}
	
	// Name:		getQueryOption
	// Description:	Get key value from message queryOptions 
	// Arguments:	key - Key in queryOptions to get value for
	// Return:		"" if key does not exists in queryOption
	private String getQueryOption(String key) {
		String value = "";
		if (queryOptions.has(key))
			value = queryOptions.get(key).getAsString();
		return value;
	}
	
	// Name:		handleOptions
	// Description:	Return value for requested key in queryOptions, solves tree structure in ecvents
	// Arguments:	newEvent - Event to search for values in
	//				key - Key to serch for in queryOptions
	// Return:		"" if key does not exists in queryOption
	private JsonObject handleOptions(JsonObject newEvent, String key) {
		JsonObject item = null;
		JsonArray fields = null;

		if (queryOptions.has(key)) {
			if (queryOptions.get(key).isJsonArray()) {
				fields = queryOptions.get(key).getAsJsonArray();
			}else {
				fields = new JsonArray();
				fields.add(queryOptions.get(key));
			}
		}else {
			logger.info("QueryOption: " + key + " does not exist in subscription message queryOptions!!");
			return item;
		}
		
		return getEventKeyValue(newEvent, fields);
		
	}
	
	// Name:		getEventKeyValue
	// Description:	Get the value of Json message keys
	// Arguments:	event - event message to get values from
	//				fields - JsonArray with message keys to search for
	// Return:		JsonObject with message keys and the respective values
	private JsonObject getEventKeyValue(JsonObject event, JsonArray fields) {
		JsonObject item = new JsonObject();
		
		for (int i = 0; i < fields.size(); i++) {
			String fieldValue = fields.get(i).getAsString();
		    String[] values = fieldValue.split("\\.");
			JsonObject current = event;
            JsonElement valueElement = new JsonPrimitive("");
            for(String value : values) {
                if (current.has(value)) {
                    JsonElement element = current.get(value);
                    if (element.isJsonObject()) {
                        current = element.getAsJsonObject();
                    } else {
                        valueElement = element;
                    }
                }else {
                    valueElement = new JsonPrimitive("UNKNOWN");
                }
            }
            item.add(fieldValue, valueElement);
		}
		return item;
	}
	
	// Name:		changeNodeStatus
	// Description:	Changes the status of the rootNode when an eiffelJobFinischedEvent or eiffelJobModifiedEvent arrived
	// Arguments:	node - RootNode
	//				status - Status string to be changed in the rootNode
	// Return:		-
	private void changeNodeStatus(JsonObject node, JsonObject event, String status) {
		logger.debug("Entering changeNodeStatus");
		JsonObject information = handleOptions(event, "information");
		
		if (information == null)
			information = new JsonObject();
		
		node.remove("information");
		node.add("information", information);
		node.remove("status");
		node.addProperty("status", status);
	}

	// Name:		changeNodeAssociatedEvents
	// Description:	Changes the associatedEvents when a new associated event arrives
	// Arguments:	node - RootNode
	// Return:		-
	private void changeNodeAssociatedEvents(JsonObject node) {
		logger.debug("Entering changeNodeAssociatedEvents");
		String id = node.get("uniqueId").getAsString();
		JsonArray relation = getRelation(id);
		node.remove("associatedEvents");
		node.add("associatedEvents", relation);
	}

	// Name:		addConnections
	// Description:	Add new Nodes to the connections key in a node
	// Arguments:	rootNode - Node to be updated
	// Return:		-
	private void addConnections(JsonObject node, String newId, String type) {
		logger.debug("Entering addConnections");
		JsonObject conn = new JsonObject();
		if (queryOptions.has("includeConnections")) {
			if (!queryOptions.get("includeConnections").getAsBoolean())
				return;
		}else {
			return;
		}
		if (node == null) {
			logger.debug("No defined Node supplied, possibly the first node in a dag!");
			return;
		}
		
		JsonArray conns = node.get("connection").getAsJsonArray();

		//		long distance = timeDistance(newEvent, rootEvent, TimeUnit.SECONDS);

		conn.addProperty("id", newId);
		conn.addProperty("type", type);
		//		conn.addProperty("distance", distance);
		conns.add(conn);
	}

	// Name:		getLocalEventByUniqueId
	// Description:	
	// Arguments:	dag - Directed Acyclic Graph  array
	//				id - id of the event to find in items array
	//				type - type of id to search for
	// Returns:		Event matching search string
	private JsonObject getLocalEventByUniqueId(JsonArray dag, String id) {
		logger.debug("Entering getLocalEventByUniqueId");
		JsonObject temp = null;

		for(int i = 0; i < dag.size(); i++) {
			temp = dag.get(i).getAsJsonObject();
			if(id.equals(temp.get("uniqueId").getAsString()))
				return temp;
		}
		return null;
	}

	// Name:		getLocalEventByAssociatedEvent
	// Description:	
	// Arguments:	dag - Directed Acyclic Graph  array
	//				id - id of the event to find in items array
	//				type - type of id to search for
	// Returns:		Event matching search string
//	private JsonObject getLocalEventByAssociatedEvent(JsonArray dag, String id) {
//		logger.debug("Entering getLocalEventByUniqueId");
//		JsonObject temp = null;
//
//		for(int i = 0; i < dag.size(); i++) {
//			temp = dag.get(i).getAsJsonObject();
//			if(temp.get("associatedEvents").getAsJsonArray().toString().matches(id))
//				return temp;
//		}
//		return null;
//	}
	
	// ============================= Handling of node relations Hashmap ====================

	// Name:		addRelation
	// Description:	
	// Arguments:	rootId - rootId for node
	//				relationId - relationId for relation node
	// Returns:		-
	private void addRelation(String rootId, String relationId, JsonArray dag) {
		logger.debug("Entering addRelation");
		String ids = "";
		if (nodeRelations.containsKey(rootId)) {
			if (!nodeRelations.get(rootId).matches(".*?" + relationId + ".*?")) {
				ids = nodeRelations.get(rootId);
				nodeRelations.remove(rootId);
				nodeRelations.put(rootId, ids.concat("," + relationId));
			}
		}else {
			nodeRelations.put(rootId, relationId);
		}
		JsonObject node = getLocalEventByUniqueId(dag, rootId);
		if (node != null)
			changeNodeAssociatedEvents(node);
	}

	// Name:		getRelation
	// Description:	
	// Arguments:	id - id for requesting node
	// Returns:		JsonArray with relation nodes
	private JsonArray getRelation(String id) {
		logger.debug("Entering getRelation");
		JsonArray relation = new JsonArray();
		if (nodeRelations.containsKey(id)) {
			Object temp = null;
			JsonParser jsonParser = new JsonParser();
			temp = jsonParser.parse("[" + nodeRelations.get(id) + "]");
			relation = (JsonArray) temp;
		}
		return relation;
	}
	
	// Name:		checkRelation
	// Description:	Checks if a specific Id exixts i the relations hash
	// Arguments:	id - id for requesting node
	// Returns:		true if the id exists in the actual subscription
	private Boolean checkRelation(String id) {
		logger.debug("Entering checkRelation");
		Iterator<Map.Entry<String, String>> iterator = nodeRelations.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			if (id.equals(entry.getKey()) || entry.getValue().matches(".*?" + id + ".*?"))
					return true;
		}
		return false;
	}

	// ===================== Handling of time =====================================

	// Name:		isValidTime 
	// Description:	Checks if live data falls within time specs. provided
	// Arguments:	event - the event to be examined
	//				startTime - StartTime from subscription
	//				endTime - EndTime from subscription
	// Returns:		true if within time specs. else false
	
	private Boolean isValidTime(JsonObject event, String start, String end) {
		logger.debug("Entering isValidTime");
		if (start.isEmpty())
			start = "0000-00-00";
		if (end.isEmpty())
			end = "9999-12-31";
		
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = dateFormat.parse(start);
			Date endDate = dateFormat.parse(end);
			Date eventDate = dateFormat.parse(event.get("eventTime").getAsString().replaceAll("'T'.*", ""));
			
			if ((eventDate.before(endDate) && eventDate.after(startDate)) || eventDate.equals(endDate) || eventDate.equals(startDate))
				return true;
		} catch (ParseException e){
			e.printStackTrace();
		}
		return false;
	}
	
	// Name:		timeDistance - Calculate time distance between two events
	// Description:	
	// Arguments:	event1 - Newest event to calculate
	//				event2 - Oldest event to calculate
	//				timeUnit - Unit of timedifference to returned from method
	//							ex) TimeUnit.SECONDS
	// Returns:		distance in the specified unit
//	private long timeDistance(JsonObject event1, JsonObject event2, TimeUnit timeUnit) {
//		logger.debug("Entering timeDistance");
//		long distance = 0;
//		try {
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
//			Date date1 = dateFormat.parse(event1.get("eventTime").getAsString());
//			Date date2 = dateFormat.parse(event2.get("eventTime").getAsString());
//			distance = date1.getTime() - date2.getTime();
//		} catch (ParseException e){
//			e.printStackTrace();
//		}
//		return timeUnit.convert(distance, TimeUnit.MILLISECONDS);
//	}

	// ==================== erhandler wrapper methods ==================================

	private JsonArray getDownstreamEventFromEr(String rootId) {
		logger.debug("Entering getDownstreamEventFromEr");
		sleep(500); // Time in milliseconds
		String json = eventRepository.findDownstream(rootId);
		logger.debug("Json: " + json);
		JsonObject jsonObj = isValidJson(json);
		if (jsonObj == null)
			return null;
		if (jsonObj.getAsJsonArray("items").size() == 1) {
			logger.debug("No downstream events found for: " + rootId);
			return null; // only rootEvent returned, no downstream events
		}
		return jsonObj.getAsJsonArray("items");
	}

	private JsonArray getUpstreamEventFromEr(String rootId) {
		logger.debug("Entering getUpstreamEventFrom");
		sleep(500); // Time in milliseconds
		String json = eventRepository.findUpstream(rootId);
		logger.debug("Json: " + json);
		JsonObject jsonObj = isValidJson(json);
		if (jsonObj == null)
			return null;
		if (jsonObj.getAsJsonArray("items").size() == 1) {
			logger.debug("No upstream events found for: " + rootId);
			return null; // only rootEvent returned, no downstream events
		}
		return jsonObj.getAsJsonArray("items");
	}

	// ============================= Misc methods ===================================

	// Name:		isValidJson
	// Description:	Check if it is a valid Json element
	// Arguments:	json - Json string to check
	// Return:		The correct Json string or null if faulty
	private JsonObject isValidJson(String json) {
		JsonObject jsonObj = null;
	    try {
	    	jsonObj = new JsonParser().parse(json).getAsJsonObject();
	    } catch(JsonParseException e) {
	    	logger.error("Malformed json detected: " + json);
	        return null;
	    }
	    return jsonObj;
	}
	
	private JsonArray sortDagByEventTime(JsonArray dag) {
		logger.debug("Entering sortDagByEventTime");
		JsonArray tmpDag = new JsonArray();
		Boolean sortOrder = true;
		if (queryOptions.has("sortOrder")) {
			if ("descending".equals(queryOptions.get("sortOrder").getAsString())) {
				sortOrder = true;
			}else if ("ascending".equals(queryOptions.get("sortOrder").getAsString())) {
				sortOrder = false;
			}
		}
		
		if (nodeTime == null)
			return dag;
		LinkedHashMap<?, ?> sortedHashMap = sortHashMapByValues(nodeTime, sortOrder); // false: Ascending true: Descending

		Iterator<?> it = sortedHashMap.keySet().iterator();

		while(it.hasNext()) {
			String key = it.next().toString();
			for (int i=0; i < dag.size(); i++) {
				String id = dag.get(i).getAsJsonObject().get("id").getAsString();
				if (key.equals(id)) {
					tmpDag.add(dag.get(i).getAsJsonObject());
				}
			}
		}
		return tmpDag;
	}

	// Name:		sortHashMapByValues
	// Description:	Sorts a HashMap on values
	// Arguments:	passedMap - HashMap to sort
	//				order - Sort order (true: Descending false: Ascending)
	// Return:		sorted HashMap
	public LinkedHashMap<Integer, String> sortHashMapByValues(HashMap<Integer, String> passedMap, Boolean order) {
		logger.debug("Entering LinkedHashMap");
		List<Integer> mapKeys = new ArrayList<Integer>(passedMap.keySet());
		List<String> mapValues = new ArrayList<String>(passedMap.values());

		if (order) {
			Collections.sort(mapValues, Collections.reverseOrder());
			Collections.sort(mapKeys, Collections.reverseOrder());
		}else {
			Collections.sort(mapValues);
			Collections.sort(mapKeys);
		}
		LinkedHashMap<Integer, String> sortedMap = new LinkedHashMap<Integer, String>();

		Iterator<String> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Object val = valueIt.next();
		    Iterator<Integer> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				String comp1 = passedMap.get(key).toString();
				String comp2 = val.toString();
				if (comp1.equals(comp2)){
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put((Integer)key, (String)val);
					break;
				}
			}
		}
		return sortedMap;
	}

	private void sleep(Integer time) {
		try {
			TimeUnit.MILLISECONDS.sleep(time);
		}catch (InterruptedException e1) {
		}
	}
	
//	private JsonObject generateResponse(Integer code) {
//		JsonObject eventBody = new JsonObject();
//		eventBody.addProperty("code", code);
//		eventBody.addProperty("error", responseCodes.get(code));
//		
//		return eventBody;
//	}
}
