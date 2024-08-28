package com.ericsson.eiffel.ve.api.data.query;

import com.nebhale.jsonpath.JsonPath;

/**
 * Representation of one VE query condition on format &lt;key&gt;=&lt;value&gt;.
 * This can be used to match against a JSON String.
 * @author xdanols
 *
 */
public class QueryCondition {
	private final String key;
	private final String value;
	private final boolean negated;

	/**
	 * Constructor for QueryCondition, takes in a condition in String format.
	 * @param condition String with a query condition
	 */
	public QueryCondition(String condition) {
		if(condition.startsWith("!")) {
			negated = true;
			condition = condition.substring(1);
		}
		else {
			negated = false;
		}
		
		if(condition.contains("=")) {
			key = condition.substring(0, condition.indexOf("="));
			value = condition.substring(condition.indexOf("=") + 1);
		}
		else {
			key = condition;
			value = null;
		}
	}

	/**
	 * Get the key of the condition
	 * @return Key as String
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get the value of the condition
	 * @return Value as String
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get if the condition is negated (marked with !)
	 * @return True if negated, false otherwise
	 */
	public boolean isNegated() {
		return negated;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		if(negated)
			result.append("!");
		
		result.append(key);
		
		if(value != null) {
			result.append("=");
			result.append(value);
		}
		
		return result.toString();
	}
	
	/**
	 * Match this condition towards a JSON String.
	 * @param json JSON String to match against
	 * @return True if condition matches string, false otherwise
	 */
	public boolean matches(String json) {
		JsonPath path = JsonPath.compile("$."+key);
        boolean result;
		
		Object read = path.read(json, Object.class);
        if (read == null) {
            result = false;
        } else if (value == null) {
            result = true;
        } else {
            result = read.toString().equals(value);
        }
        
        return isNegated() ? !result : result;
	}
	
	/**
	 * Return this QueryCondition as an ER Query condition. Observe that the
	 * ER does not support keys without values, so conditions checking for a
	 * key to exist will be ignored.
	 * @return An ER compatible String of this condition, empty if only &lt;key&gt;
	 */
	public String getERQueryCondition() {
		if(value == null)
			return "";

		StringBuilder result = new StringBuilder();

		result.append(key);
		
		if(isNegated())
			result.append("!");
		
		result.append("=");
		result.append(value);
		
		return result.toString();
	}
}
