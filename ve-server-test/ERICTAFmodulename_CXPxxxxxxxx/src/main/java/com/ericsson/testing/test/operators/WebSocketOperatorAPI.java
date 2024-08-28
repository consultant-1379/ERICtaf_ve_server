package com.ericsson.testing.test.operators;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import io.socket.*;

import org.apache.log4j.Logger;

import com.google.gson.JsonElement;

public class WebSocketOperatorAPI implements IOCallback, WebSocketOperator, Runnable {

	private List<JsonElement> messages = new ArrayList<JsonElement>();
	
	private SocketIO socket;
	private int count = 0;
	private int port = 8585;
	private String host = "";
	private String subscription = "";
	private boolean saveMessages = false;
	
	Logger log = Logger.getLogger(WebSocketOperatorAPI.class);
	
	public WebSocketOperatorAPI(String host,int port,String topic, String model, String uuid) {
		this.host = host;
		this.port = port;
		this.subscription = this.formatSubscription(topic, model, uuid);
		log.info("New Client");
	}
	
	 public void createConnection(String host, int port) {    
		    try {	    	
		    	this.socket = new SocketIO();
		    	this.socket.connect("http://"+host+":"+port+"/",  this);
		    		    		    	
		    }
		    catch(MalformedURLException io) {
		    	log.debug("Error "+io.toString());
		    } 
	    }
		
	    
	    public void sendSubscription(String msg) {    	
	    		this.socket.emit("subscribe", msg);
	    	    log.debug("Subscription "+msg);			
	    
	    }
	    
		public void sendMessage(String msg) {
	    	try{			
	    		this.socket.send(msg);
			}
			
		    catch(Exception e) {
		    	log.debug(e.toString());
		    }
			
		}
		
		public boolean isConnected() {
			return this.socket.isConnected();
		}
	    
	    public List<JsonElement> receievedMessages(){  	    	    	
	    	log.debug("Messages Received: "+this.count);
	    	return this.messages;
	    }
	    	   
	    public void close() {
	    	try{    		
	    		this.socket.disconnect();
	    		this.socket = null;
	    	}
	    	catch(Exception io) {
	    		log.debug("webSocket connection not closed");
	    	}
	    }
	    
	    private String formatSubscription(String topic, String model, String uuid) {
	    	return "{ \"topic\": { \"eventType\" : \"" + topic + "\"}, \"model\": \"" + model + "\", \"uuid\": \"" + uuid + "\"}";
	    }

		@Override
		public void onConnect() {
			log.debug("Connection established "+this.socket.isConnected());
			
		}


		@Override
		public void onDisconnect() {
			log.debug("Connection Disconnected");
				
		}


		@Override
		public void onError(SocketIOException arg0) {
			log.debug("Error "+arg0.toString());
			arg0.printStackTrace();
	
		}

		@Override
		public void onMessage(String arg0, IOAcknowledge arg1) {			
			log.debug("Message from server "+arg0.toString());
			
		}


		@Override
		public void on(String arg0, IOAcknowledge arg1, JsonElement... arg2) {
			 if(arg2.length > 0) {
				//log.debug("Received message from Server with EventId Received "+(arg2[0].getAsJsonObject().get("message")).getAsJsonObject().get("eventId"));
				if(this.saveMessages) {
					for(JsonElement e : arg2) {		
						this.messages.add(e.getAsJsonObject().get("message"));	
					}
				}			
				this.count += arg2.length;	
			}
		}
			

		@Override
		public void onMessage(JsonElement arg0, IOAcknowledge arg1) {
			this.messages.add((arg0.getAsJsonObject().get("message")));
			log.debug("Message added "+arg0.toString());
			this.count++;
			
			
		}


		@Override
		public void run() {
			createConnection(this.host, this.port);   	
	    	sendSubscription(this.subscription);
	    	while(!Thread.currentThread().isInterrupted()) {
	    		//wait till finished
	    		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
	    	}
	    	log.debug("Messages Received: "+this.count);
	    	close();
			
		}
		
		

	  
}
