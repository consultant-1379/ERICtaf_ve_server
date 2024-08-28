package com.ericsson.eiffel.ve.application.handler;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.plugins.VEMessageHandler;
import com.ericsson.eiffel.ve.web.VEService;

public class LiveDataSubscriptionHandler implements VEMessageHandler {

	private final String RESOURCE_NAME = "ve:livedata/subscriptions";
	
	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public void handle(String sessionId, RESTEvent event) {
        VEService veService = Bootstrap.getInstance().getVEService();
        if("PUT".equals(event.getMethod()))
        	veService.subscribe(sessionId, event);
        else if("DELETE".equals(event.getMethod()))
        	veService.unsubscribe(sessionId, event);
	}

	@Override
	public RESTEvent handleWithResponse(RESTEvent event) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
