package com.ericsson.eiffel.ve.api.data.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a list of VE queries, where one must be fulfilled for the
 * query list to match.
 * @author xdanols
 *
 */
public class QueryList extends ArrayList<Query> {
	
	private static final long serialVersionUID = -7711400628234897153L;

	/**
	 * Private constructor, use parseQueryString to create a QueryList object.
	 */
	private QueryList() {
	}
	
	/**
	 * Parses a queryString and returns a new QueryList with one Query object for each part
	 * of the query separated by OR, e.g. QUERY1||QUERY2||QUERY3.
	 * Observe that this is the only way to create a QueryList object.
	 * @param queryString
	 * @return A list of Query objects
	 */
	public static QueryList parseQueryString(String queryString) {
		QueryList result = new QueryList();

		// Splits on ||, regular expression requires \\ before each |
		for(String query : queryString.split("\\|\\|"))
			result.add(new Query(query));
		
		return result;
	}
	
	/**
	 * Matches a JSON String towards the query represented by the current QueryList instance.
	 * @param json JSON String to match against
	 * @return True if one of the queries match, false otherwise
	 */
	public boolean matches(String json) {
		for(Query q : this)
			if(q.matches(json))
				return true;
		
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String prefix = "";
		
		for(Query q : this) {
			builder.append(prefix);
			builder.append(q.toString());
			prefix = "||";
		}
		
		return builder.toString();
	}
	
	/**
	 * Convert this VE Query list to an Event Repository query list.
	 * 
	 * WARNING: Since the Event Repository query language does not support checking that a
	 * key exists in a message (implied if only a key is given without a value in the VE
	 * Query language), all keys without values are left out of the returned query from
	 * this method.
	 * 
	 * @return A list of ER query strings
	 */
	public List<String> getERQueryList() {
		List<String> result = new ArrayList<String>();

		for(Query query : this) {
			result.add(query.getERQuery());
		}
		
		return result;
	}
}
