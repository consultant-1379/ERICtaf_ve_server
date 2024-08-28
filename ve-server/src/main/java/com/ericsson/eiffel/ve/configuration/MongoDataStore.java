package com.ericsson.eiffel.ve.configuration;

import java.net.UnknownHostException;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.bson.types.ObjectId;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;


/**
 * Database specific handling for the Mongo datastore.
 */

public class MongoDataStore {

	private static final EiffelLog log = new JavaLoggerEiffelLog(MongoDataStore.class);

	private final JsonParser jp = new JsonParser();

	private final String hostname;
	ProducerTemplate producerTemplate;

	private static final String dbName="veconf";
	private static final String dashboardCollName = "dashboards";
	private static final String viewCollName = "views";
	private static final int retryCount=5;
	private static final int retryInterval=100;

	private static final String DASHBOARD_STORE = "direct:storeDashboard";
	private static final String DASHBOARD_FINDBYID = "direct:findDashboardById";
	private static final String DASHBOARD_FINDALL = "direct:findAllDashboards";
	private static final String DASHBOARD_REMOVE = "direct:removeDashboardById";

	private static final String VIEW_STORE = "direct:storeView";
	private static final String VIEW_FINDBYID = "direct:findViewById";
	private static final String VIEW_FINDALL = "direct:findAllViews";
	private static final String VIEW_REMOVE = "direct:removeViewById";

	/**
	 * Class constructor. Handles basic setup of the database.
	 * @throws UnknownHostException 
	 */
	public MongoDataStore(final String hostname)   {
		this.hostname = hostname;
	}
	
	public void start() throws VEConfigurationException{
		MongoClient client;
		//		final List<ServerAddress> servers = getConfiguredServers(configuration, properties);
		try {
			client = new MongoClient(hostname);
			CamelContext context = setupCamelRoutes(client);
			producerTemplate = context.createProducerTemplate();
		} catch (UnknownHostException e) {
			log.error("Could not connect to MongoDB due to UnknownHostException: " +e.getMessage());
			throw new VEConfigurationException("Could not connect to MongoDB due to unknown host", e);
		}
	}


	/**
	 * Update of existing dashboard
	 * 
	 * @param json Json to be stored in configuration
	 * @return Updated DashboardConfiguration if store was success, else empty string
	 */
	public String putDashboard(final String json) {
		JsonParser parser = new JsonParser();
		JsonObject object = parser.parse(json).getAsJsonObject();
		JsonObject dashBoard = findByIdToJson(object.get("_id").getAsString(), DASHBOARD_FINDBYID);
		if (dashBoard== null) {
			log.info("PUT: id not found");
			return "";
		} else {
			storeJson(json, DASHBOARD_STORE);
			log.info("PUT: " + json );
			return getDashboard(object.get("_id").getAsString());
		}
	}

	/**
	 * Creation of new dashboard
	 * @param json Json to be stored in configuration
	 * @return New DashboardConfiguration
	 */
	public String postDashboard(final String json) {
		ObjectId id = ObjectId.get();
		JsonParser parser = new JsonParser();
		JsonObject o = (JsonObject)parser.parse(json);
		o.addProperty("_id", id.toString());
		String storeJson = o.toString();
		
		log.info("POST: " + storeJson );
		
		storeJson(storeJson, DASHBOARD_STORE);
		return (getDashboard(id.toString()));

	}

	/**
	 * Get dashbaord
	 * @param id of dashboard
	 * @return Complete json with dashboard configuration
	 */
	public String getDashboard(final String id){
		log.info("GET: " + id );

		JsonObject json = findByIdToJson(id, DASHBOARD_FINDBYID);
		if (json== null) {
			return null;
		} else {
			return (json.toString());
		}
	}


	/**
	 * Get dashboards
	 * @param Options in a Map. These are used as filters with two exceptions.
	 *              Key 'ids' has a list of ids (comma separated)
	 *              Key 'outputType' specifies how much shall be outputted to the client.
	 *              Value 'full' will give the complete configuration otherwise only a list of
	 *              _id and title.
	 * @return Complete json with dashboard configuration
	 */
	public String getDashboards(final Map<String,String> options){
		log.info("GET: " + options.toString() );

		return (findAllToJson(DASHBOARD_FINDALL, options).toString());
	}

	/**
	 * Delete dashboard
	 * @param id of dashboard
	 * @return Boolean result if delete was success
	 */
	public boolean deleteDashboard(final String id){
		JsonObject json = findByIdToJson(id, DASHBOARD_FINDBYID);
		if (json != null)
		{
			return (removeById(id, DASHBOARD_REMOVE));	
		}
		else 
		{
			return false;
		}
	}


	/**
	 * Update of existing view
	 * 
	 * @param json Json to be stored in configuration
	 * @return Updated ViewConfiguration if store was success, else empty string
	 */
	public String putView(final String json) {
		JsonParser parser = new JsonParser();
		JsonObject object = parser.parse(json).getAsJsonObject();
		JsonObject view = findByIdToJson(object.get("_id").getAsString(), VIEW_FINDBYID);
		if (view== null) {
			log.info("PUT: id not found");
			return "";
		} else {
			storeJson(json, VIEW_STORE);
			log.info("PUT: " + json );
			return getView(object.get("_id").getAsString());
		}

	}

	/**
	 * Creation of new view
	 * @param json Json to be stored in configuration
	 * @return New ViewConfiguration
	 */
	public String postView(final String json) {
		ObjectId id = ObjectId.get();
		JsonParser parser = new JsonParser();
		JsonObject o = (JsonObject)parser.parse(json);
		o.addProperty("_id", id.toString());
		String storeJson = o.toString();
		log.info("POST: " + storeJson );
		storeJson(storeJson, VIEW_STORE);
		return (getView(id.toString()));
	}

	/**
	 * Get view
	 * @param id of view
	 * @return Complete json with view configuration
	 */
	public String getView(final String id){
	
		JsonObject json = findByIdToJson(id, VIEW_FINDBYID);
		if (json== null) {
			return null;
		} else {
			return (json.toString());
		}
	}

	/**
	 * Get view
	 * @param Options in a Map. These are used as filters with two exceptions.
	 *              Key 'ids' has a list of ids (comma separated)
	 *              Key 'outputType' specifies how much shall be outputted to the client.
	 *              Value 'full' will give the complete configuration otherwise only a list of
	 *              _id and title.
	 * @return Complete json array with view configuration
	 */
	public String getViews(final Map<String,String> options){
		return (findAllToJson(VIEW_FINDALL, options).toString());
	}

	/**
	 * Delete view
	 * @param id of view
	 * @return Boolean result if delete was success
	 */
	public boolean deleteView(final String id){

		JsonObject json = findByIdToJson(id, VIEW_FINDBYID);
		if (json != null)
		{
			return (removeById(id, VIEW_REMOVE));	
		}
		else 
		{
			return false;
		}
	}

	private CamelContext setupCamelRoutes(MongoClient client) throws VEConfigurationException  {
		final SimpleRegistry reg = new SimpleRegistry();

		// Enable exceptions on failure
		client.setWriteConcern(WriteConcern.SAFE);
		reg.put("VEDb", client);

		// Initialize Camel
		final CamelContext context = new DefaultCamelContext(reg);
		try {
			context.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {
					onException(Exception.class).redeliveryDelay(retryInterval).maximumRedeliveries(retryCount);
					from(DASHBOARD_STORE)
					.to("mongodb:VEDb?database=" + dbName + "&collection=" + dashboardCollName + "&operation=save");
					from(DASHBOARD_FINDBYID)
					.to("mongodb:VEDb?database=" + dbName + "&collection=" + dashboardCollName + "&operation=findById");
					from(DASHBOARD_FINDALL)
					.to("mongodb:VEDb?database=" + dbName + "&collection=" + dashboardCollName + "&operation=findAll");
					from(DASHBOARD_REMOVE)
					.to("mongodb:VEDb?database=" + dbName + "&collection=" + dashboardCollName + "&operation=remove");

					from(VIEW_STORE)
					.to("mongodb:VEDb?database=" + dbName + "&collection=" + viewCollName + "&operation=save");
					from(VIEW_FINDBYID)
					.to("mongodb:VEDb?database=" + dbName + "&collection=" + viewCollName + "&operation=findById");
					from(VIEW_FINDALL)
					.to("mongodb:VEDb?database=" + dbName + "&collection=" + viewCollName + "&operation=findAll");
					from(VIEW_REMOVE)
					.to("mongodb:VEDb?database=" + dbName + "&collection=" + viewCollName + "&operation=remove");
				}
			});
			log.info("Starting Camel context");
			context.start();
		} catch (Exception e) {
			log.error("Camel route(s) could not be started. " + e.getMessage());
			throw new VEConfigurationException("Camel route(s) could not be started.", e);
		}

		return context;
	}

	private boolean storeJson(final String json, final String route) {
		try {
			DBObject result = producerTemplate.requestBody(route, json, DBObject.class);
			log.info("RESULT: " + result.toString());
			return true;
		} catch(CamelExecutionException e){
			log.error("Could not store: " + json + " on route: "+ route + " Due to error: " +e.getMessage());
			return false;
		}
	}

	private JsonObject findByIdToJson(final String id, final String route) throws CamelExecutionException {
		final DBObject result = producerTemplate.requestBody(route, id, DBObject.class);
		if (result != null){
			try {
				log.info(result.toString());
				return jp.parse(result.toString()).getAsJsonObject();
			} catch (JsonSyntaxException e) {
				log.error("Could not parse json result from MongoDB using route " + route + ". Message: "+ e.getMessage());
				return null;
			} 
		} else {
			log.warn("Failed to find id " + id + " using route " + route);
			return null;
		}
	}

	private JsonArray findAllToJson(final String route, Map <String,String> options) throws CamelExecutionException {
		BasicDBObject optionQuery = null;
		BasicDBObject idQuery = null;
		boolean fullOutput = false;
		for (Map.Entry<String, String> entry : options.entrySet()) {
			if (entry.getKey().equals("outputType")){
				if (entry.getValue().equals("full")){
					fullOutput = true;
				}
			} else if (entry.getKey().equals("ids")){
				String[] ids = entry.getValue().split(",");
				BasicDBList queryList = new BasicDBList();
				for (String id : ids) {
					queryList.add(new BasicDBObject("_id", id));
				}
				idQuery = new BasicDBObject("$or", queryList);

			} else {
				if (optionQuery == null) {
					optionQuery = new BasicDBObject(entry.getKey(), entry.getValue());
				} else {
					optionQuery.append(entry.getKey(), entry.getValue());
				}
			}
		}

		BasicDBObject query = null;
		if (idQuery != null){
			if (optionQuery == null){
				query = idQuery;
			} else {
				BasicDBList andQueryList = new BasicDBList();
				andQueryList.add(idQuery);
				andQueryList.add(optionQuery);
				query = new BasicDBObject("$and", andQueryList);
			} 
		} else {
			query = optionQuery;
		}

		Object result;
		if (fullOutput){
			result = producerTemplate.requestBody(route, query);
		} else {
			DBObject fieldFilter = BasicDBObjectBuilder.start().add("_id", 1).add("title", 1).get();
			result = producerTemplate.requestBodyAndHeader(route, query, MongoDbConstants.FIELDS_FILTER, fieldFilter);
		}
		if (result != null){
			try {
				return jp.parse(result.toString()).getAsJsonArray();
			} catch (JsonSyntaxException e) {
				log.error("Could not parse json result from MongoDB using route " + route + ". Message: "+ e.getMessage());
				return null;
			} 
		} else {
			log.debug("No results found using route: " + route);
			return new JsonArray();
		}
	}

	private boolean removeById(final String id, final String route) {
		DBObject body = new BasicDBObject("_id", id);
		try {
			Object result = producerTemplate.requestBody(route, body);
			log.info(result.toString());
		}
		catch (CamelExecutionException e){
			log.error("Could not delete id:" + id + " using route: " + route + ". Message: " + e.getMessage());
			return false;
		}
		log.info("Deleted id " + id + "using route: " + route);
		return true;
	}

}