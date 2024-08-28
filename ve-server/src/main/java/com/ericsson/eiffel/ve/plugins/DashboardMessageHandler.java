package com.ericsson.eiffel.ve.plugins;

import java.util.Map;

public interface DashboardMessageHandler {
	String getMessageName();

	void handle(String sessionId, Map<String,String> args);
}
