package com.ericsson.eiffel.ve.actor.message;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;

public class VEHistoricalData implements VEMessage {

	private final String sessionId;
	private final String filter;
	private static final EiffelLog logger = new JavaLoggerEiffelLog(VEHistoricalData.class);

	
	
	public VEHistoricalData(final String sessionId, final String filter) {
		logger.debug("Creating new HistoricalData message with session : " + sessionId + " and filter: " + filter);
		this.sessionId = sessionId;
		this.filter = filter;
	}
	
	@Override
	public String getSessionId() {
		return sessionId;
	}
	
	public String getFilter(){
		return filter;
	}

	@Override
	public String toString(){
		return getFilter();
	}
}
