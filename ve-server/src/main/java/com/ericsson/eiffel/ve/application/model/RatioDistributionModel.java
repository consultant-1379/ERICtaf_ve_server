package com.ericsson.eiffel.ve.application.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.api.data.query.QueryList;
import com.ericsson.eiffel.ve.api.internal.EventRepositoryAccessor;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.api.internal.VEConnection;
import com.ericsson.eiffel.ve.api.response.ResponseHandler;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.infrastructure.config.Settings;
import com.ericsson.eiffel.ve.plugins.er.EventRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nebhale.jsonpath.JsonPath;

public class RatioDistributionModel implements VEModel, Runnable {

    private static final EiffelLog logger = new JavaLoggerEiffelLog(RatioDistributionModel.class);
	private QueryList queryList;
	private RESTEvent subscriptionEvent = null;
	private VEConnection connection = null;
    private final JsonObject jsonObject;
    private boolean changed = false;
    private ScheduledThreadPoolExecutor executor = null;
    private String jsonReport;

    private Date startDate;
    private Date endDate;
    private String base;
    private SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
    private EventRepositoryAccessor eventRepository;
	private JsonParser parser;
	private Settings settings;
    
    public RatioDistributionModel() {
    	logger.debug("Create new RatioDistributionModel");
        this.jsonObject = generateRatioDistributionModelObject();
		parser = new JsonParser();
		settings = Bootstrap.getInstance().getSettings();
    }

    @Override
	public void initSubscription(RESTEvent subscriptionEvent, VEConnection connection) {

    	this.subscriptionEvent=subscriptionEvent;
    	logger.debug("subscriptionEvent: "+subscriptionEvent.toString());

		this.connection = connection;
		updateSubscription(subscriptionEvent);
    	int updateInterval = subscriptionEvent.getEventBody().get("updateInterval").getAsInt();

    	getQueryData(subscriptionEvent);
    	if(eventRepository != null){
    		List<String> querys = createQuery();
    		String json = ""; 
    		for(String query : querys){
    			json = eventRepository.findEvents(query);
    			updateHistoricalData(json, this.jsonObject);
    		}		
			this.jsonReport = ResponseHandler.generateUpdateMessageString(subscriptionEvent, this.jsonObject);
    	}  	
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(this, updateInterval, updateInterval, TimeUnit.SECONDS);
    }
    
    private void updateHistoricalData(String json, JsonObject jsonObject) {
    	JsonObject jSonObj = parser.parse(json).getAsJsonObject();
		if(jSonObj.has("items") && jSonObj.get("items").isJsonArray()){
			JsonArray events = jSonObj.get("items").getAsJsonArray();
			for (int i = 0; i < events.size(); i++) {
				String label = JsonPath.read("$."+base, events.get(i).toString(), String.class);
				if (label!=null){
					addEvent(jsonObject.getAsJsonArray("items"), label);
					changed = true;
				}
			}	
		}
    }
 
    private List<String> createQuery(){
    	List<String> querys = this.queryList.getERQueryList();
    	
    	for (String query : querys) {
		
	    	if(this.startDate != null)
	    		query = query+"&eventTime%3e"+ft.format(startDate);
	    	if(this.endDate != null)
	    		query = query +"&eventTime%3c"+ft.format(endDate);
	    	
	    	query.replaceAll("^&", "");

    	}
    	
    	return querys;
    }
    
    public Date parseDate(String dateToParse) {
    	Date date = null;
		try {
			date = ft.parse(dateToParse);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
    }

	@Override
    public void updateSubscription(RESTEvent subscriptionEvent) {
		this.subscriptionEvent = subscriptionEvent;
		this.queryList = QueryList.parseQueryString(this.subscriptionEvent.getEventBody().get("query").getAsString());
	}

    @Override
    public void consume(String json) {
    	logger.debug("Consumed message, json: " + json);

    	String label = JsonPath.read("$."+base, json, String.class);
    	if(label != null){
    		addEvent(jsonObject.getAsJsonArray("items"), label);
    		jsonReport = ResponseHandler.generateUpdateMessageString(subscriptionEvent, jsonObject);
    		changed = true;
    	}
    }

	@Override
	public void run() {

		if(changed) {
			logger.debug("Sending a status report: "+jsonReport);
			connection.send("update", jsonReport);
		}
		changed = false;
	}
	
	@Override
    public boolean matches(String json) {
		
		Boolean ret = queryList.matches(json); 
		
		if (ret){
			JsonParser parser = new JsonParser();
			JsonObject object = parser.parse(json).getAsJsonObject();
			Date eTime = null;
			if(object.has("eventTime")){
				JsonElement eventTime = object.get("eventTime");
				eTime = parseDate(eventTime.getAsString());
			}

			if(eTime != null){
				logger.debug("ratiodistributionmodel.matches. eTime: "+eTime.getTime());	
				if(startDate != null && endDate != null){
					ret = eTime.after(startDate) && eTime.before(endDate);
				}else if(startDate != null){
					ret = eTime.after(startDate);
				}else if(endDate != null){
					ret = eTime.before(endDate);
				}
			}
		}
    	return ret;
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
	
	private JsonObject generateRatioDistributionModelObject() {
    	logger.debug("RatioDistributionModel.generateRatioDistributionModelObject ");

		JsonObject result = new JsonObject();
		result.add("modelMetaData", new JsonObject());
		result.getAsJsonObject("modelMetaData").addProperty("Type", "ratioDistribution");
		result.getAsJsonObject("modelMetaData").addProperty("Version", "1.0.0");
		result.add("items", new JsonArray());
//		{
//			"modelMetaData" : {"Type" : "ratioDistribution", "Version" : "1.2.3"}, 
//			"items" : [
//			    {"label" : "eventType1", "value" : 10},
//			    {"label" : "eventType2", "value" : 20}
//			]
//		}
		return result;
	}
	
	private void addEvent(JsonArray items, String eventType) {
		JsonObject item = null;
		for(int i = 0; i < items.size(); i++) {
			JsonObject temp = items.get(i).getAsJsonObject();
			if(eventType.equals(temp.get("label").getAsString())){
				item = temp;
			}
		}
		if(item == null) {
			item = new JsonObject();
			item.addProperty("label", eventType);
			item.addProperty("value", 1);
			items.add(item);
		}
		else {
			item.addProperty("value", item.get("value").getAsInt() + 1);
		}
	}

	@Override
	public String getModelName() {
		return getClass().getSimpleName();
	}

	private void getQueryData(RESTEvent queryEvent){
		JsonElement base = queryEvent.getEventBody().get("queryOptions").getAsJsonObject().get("base");
		JsonElement startDate = queryEvent.getEventBody().get("queryOptions").getAsJsonObject().get("startDate");
		JsonElement endDate = queryEvent.getEventBody().get("queryOptions").getAsJsonObject().get("endDate");
		
		if(base != null && !base.getAsString().isEmpty()){
			this.base=base.getAsString();
		}else{
			this.base="eventType";
		}
		
		if(startDate != null && !startDate.getAsString().isEmpty())
			this.startDate = parseDate(startDate.getAsString());
		if(endDate != null && !endDate.getAsString().isEmpty())
			this.endDate = parseDate(endDate.getAsString());

        if (eventRepository == null && settings != null)
        	if(settings.getString("eventRepository.uri") != null)
        		setEventRepositoryAccessor(new EventRepository(settings.getString("eventRepository.uri")));

	}
	
	@Override
	public String query(RESTEvent queryEvent) {
			
		getQueryData(queryEvent);
    	String json = "{}";
    	if(eventRepository != null){
    		JsonObject jsonObject = generateRatioDistributionModelObject();
    		this.queryList = QueryList.parseQueryString(queryEvent.getEventBody().get("query").getAsString());
    		List<String> querys = createQuery();

    		for(String query : querys){
    			json = eventRepository.findEvents(query);
    			updateHistoricalData(json, jsonObject);
    		}

			json  = ResponseHandler.generateUpdateMessageString(queryEvent, jsonObject);
    	}
    	return json;
	}

	@Override
	public void setEventRepositoryAccessor(
			EventRepositoryAccessor eventRepositoryAccessor) {
		 eventRepository = eventRepositoryAccessor;
	}
}
