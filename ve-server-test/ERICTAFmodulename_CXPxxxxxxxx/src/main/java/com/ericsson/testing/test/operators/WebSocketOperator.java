package com.ericsson.testing.test.operators;

import java.util.List;

public interface WebSocketOperator{
		
    
	void createConnection(String host, int port);
	
    void sendMessage(String msg);
 
    void close();

	List receievedMessages();

	void sendSubscription(String subscription);

	boolean isConnected();

}
