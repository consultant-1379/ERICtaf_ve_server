package com.ericsson.eiffel.ve.infrastructure.config;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class  Settings {

    private final Map<String, String> map;

    public Settings(Map<String, String> properties) {
        this.map = properties;
    }

    public String getValue(String key) {
        return map.get(key);
    }

    // Get list of all settings element in a specific yaml string
    public List<String> getList(String id) {
        String[] tokens = id.split("\\.");
        Map object = getObject(tokens);
        if (object == null) {
        	return null;
        }else{
        	return (List<String>) object.get(tokens[tokens.length - 1]);
        }
    }

    // Get a specific settings String value related to a specific yaml string
    public String getString(String id) {
        String[] tokens = id.split("\\.");
        Map object = getObject(tokens);
        if (object == null) {
        	return null;
        }else{
        	return (String) object.get(tokens[tokens.length - 1]);
        }   
    }

    // Get a specific Boolean value related to a specific yaml string
    public Boolean getBoolean(String id) {
        String[] tokens = id.split("\\.");
        Map object = getObject(tokens);
        if (object == null) {
        	return null;
        }else{
        	return (Boolean) object.get(tokens[tokens.length - 1]);
        }
    }

    // Get a specific Integer value related to a specific yaml string
    public java.lang.Integer getInteger(String id) {
        String[] tokens = id.split("\\.");
        Map object = getObject(tokens);
        if (object == null) {
        	return null;
        }else{
        	return (java.lang.Integer) object.get(tokens[tokens.length - 1]);
        }
    }

    private Map getObject(String[] tokens) {
        Map value = map;
        int i = 0;
        while (i < tokens.length - 1) {
            value = (Map) value.get(tokens[i]);
            i++;
        }
        return value;
    }

}
