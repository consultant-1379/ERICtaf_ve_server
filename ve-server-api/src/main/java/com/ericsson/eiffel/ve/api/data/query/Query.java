package com.ericsson.eiffel.ve.api.data.query;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the representation of a VE query with multiple conditions where all conditions
 * must be fulfilled for the query itself to be fulfilled.
 * @author xdanols
 *
 */
public class Query {
	private final List<QueryCondition> conditions;

	/**
	 * Constructor, takes in a string of conditions separated with "&&"
	 * @param conditionString Condition string
	 */
	public Query(String conditionString) {
		conditions = new ArrayList<QueryCondition>();

		for(String condition : conditionString.split("&&"))
			conditions.add(new QueryCondition(condition));
	}

	/**
	 * Get all conditions that needs to be fulfilled for this query to go through
	 * @return A list of QueryCondition objects
	 */
	public List<QueryCondition> getConditions() {
		return conditions;
	}

	@Override
	public String toString() {
		String prefix = "";
		StringBuilder result = new StringBuilder();

		for(QueryCondition qc : conditions) {
			result.append(prefix);
			result.append(qc.toString());
			prefix = "&&";
		}

		return result.toString();
	}

	/**
	 * Matches this query against a JSON String.
	 * @param json JSON String
	 * @return True if all conditions matches, false otherwise
	 */
	public boolean matches(String json) {
		for(QueryCondition condition : conditions)
			if(!condition.matches(json))
				return false;

		return true;
	}

	/**
	 * Convert this VE Query to an Event Repository query.
	 * @return An Event repository query in String format
	 */
	public String getERQuery() {
		String prefix = "";
		StringBuilder result = new StringBuilder();

		for(QueryCondition qc : conditions) {
			if(!qc.getERQueryCondition().isEmpty()) {
				result.append(prefix);
				result.append(qc.getERQueryCondition());
				prefix = "&";
			}
		}

		return result.toString();
	}
}
