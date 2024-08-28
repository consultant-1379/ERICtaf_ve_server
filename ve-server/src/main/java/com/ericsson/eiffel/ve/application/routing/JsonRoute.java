package com.ericsson.eiffel.ve.application.routing;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nebhale.jsonpath.InvalidJsonPathExpressionException;
import com.nebhale.jsonpath.JsonPath;

import java.util.concurrent.TimeUnit;


public class JsonRoute {

    private static final EiffelLog logger = new JavaLoggerEiffelLog(EiffelMessageRouter.class);

	
    private final Cache<String, Boolean> eventIdCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    private final JsonPath path;
    private final String value;
    private final String topic;

    private JsonRoute(JsonPath path, String value, String topic) {
        this.path = path;
        this.value = value;
        this.topic = topic;
    }

    public static JsonRoute parse(String topic) {
    	logger.debug("Parsing topic: '"+topic+"'");
    	String trimmedTopic = topic.endsWith(":") ? topic.substring(0, topic.length()-1) : topic;
        int colonIndex = trimmedTopic.indexOf(':');
        String pathPart;
        String value;
        if (colonIndex != -1) {
            pathPart = trimmedTopic.substring(0, colonIndex);
            value = trimmedTopic.substring(colonIndex + 1);
        } else {
            pathPart = trimmedTopic;
            value = null;
        }
        try {
            JsonPath path = JsonPath.compile("$." + pathPart);
            logger.debug("Creating new JsonRoute, path:'"+path.toString()+"', value:'"+value+"'");
            return new JsonRoute(path, value, trimmedTopic);
        } catch (InvalidJsonPathExpressionException e) {
        	logger.error("InvalidJsonPathExpressionException: "+e.getMessage());
            return null;
        }
    }
    
    public static JsonRoute parseHistoricalData(String filters) {
    	logger.debug("parsing filters: '" + filters +"'");
    	
        int colonIndex = filters.indexOf(':');
        String pathPart;
        String value;
        if (colonIndex != -1) {
        	logger.debug("Found colon at index "+colonIndex);
            pathPart = filters.substring(1, colonIndex);
            value = filters.substring(colonIndex + 1);
        } else {
        	logger.debug("No colon found");
            pathPart = filters;
            value = null;
        }
        try {
            JsonPath path = JsonPath.compile("$." + pathPart);
            logger.debug("Creating new JsonRoute, path:'"+path.toString()+"', value:'"+value+"'");
            return new JsonRoute(path, value, filters);
        } catch (InvalidJsonPathExpressionException e) {
        	logger.error("InvalidJsonPathExpressionException: "+e.getMessage());
            return null;
        }
    }

    public boolean matches(String json) {
    	logger.debug("Matching json '"+json+"' with jsonpath='"+path.toString()+"', value='"+value+"', topic='"+topic+"'");
    	
    	// Special case to match all messages
    	if("all".equals(topic) && value == null)
    		return true;
    	
        Object result = path.read(json, Object.class);
        if (result == null) {
        	logger.debug("path.read == null");
            return false;
        } else if (value == null) {
            return true;
        } else {
            return result.toString().equals(value);
        }
    }

    public String getTopic() {
        return topic;
    }

    public void addToEventHierarchy(String eventId) {
        eventIdCache.put(eventId, true);
    }

    public boolean isInEventHierarchy(String eventId) {
        return eventId != null && eventIdCache.getIfPresent(eventId) != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonRoute that = (JsonRoute) o;

        if (!path.toString().equals(that.path.toString())) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path.toString().hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
