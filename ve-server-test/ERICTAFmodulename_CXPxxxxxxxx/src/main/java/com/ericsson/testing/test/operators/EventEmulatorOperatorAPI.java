package com.ericsson.testing.test.operators;

import java.util.Random;

import com.ericsson.duraci.eiffelmessage.sending.MessageSender;
import com.ericsson.duraci.eiffelmessage.sending.exceptions.EiffelMessageSenderException;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.duraci.eiffelmessage.mmparser.clitool.EiffelConfig;
import com.ericsson.duraci.eiffelmessage.messages.events.*;
import com.ericsson.duraci.datawrappers.ResultCode;

import javax.inject.Singleton;


@Operator(context = Context.API)
@Singleton
public class EventEmulatorOperatorAPI implements EventEmulatorOperator, Runnable {

	Logger log = Logger.getLogger(EventEmulatorOperatorAPI.class);
	
	public boolean stopFlag = true;
	
	private EiffelConfig config;
	private MessageSender sender;
	private final int SECOND_IN_MILLISECONDS = 1000;
	private int sustainedMessageRate;
	private boolean varyMessageRate;
	private boolean varyMessageType;
	private int maxMessageRate;
	
	public EventEmulatorOperatorAPI() {
		this.sustainedMessageRate = Integer.parseInt(DataHandler.getAttribute("sustainedMessageRate").toString());
		this.varyMessageRate = Boolean.parseBoolean(DataHandler.getAttribute("varyMessageRate").toString());
		this.varyMessageType = Boolean.parseBoolean(DataHandler.getAttribute("varyMessageType").toString());
		this.maxMessageRate = Integer.parseInt(DataHandler.getAttribute("maxMessageRate").toString());
	}
	
	public void createSender() {
		this.sender = new MessageSender.Factory(this.config).create();
	}
	
	public void sendMessage(EiffelEvent event) throws EiffelMessageSenderException {		
	    EiffelMessage eMsg = this.sender.send(EiffelMessage.Factory.create(this.config.getDomainId(), event));
	    log.debug ("Sent message: "+eMsg.getEventId());		
	}
	
	public void sendMsgPerSecond() {
        int messageRate;
        int count = 0;
        try {        	
    		while (!Thread.currentThread().isInterrupted()) {
    			messageRate = getMessageRate();
				long endTime = System.currentTimeMillis() + SECOND_IN_MILLISECONDS;
				while (System.currentTimeMillis() < endTime) {
					if (count < messageRate) {
						sendMessage(this.eiffelEvents());
						count++;
					}
				}
				count = 0;
			}
        }
        catch(Exception e) {
        	log.error(e.toString());
        }
	}
		        
    public void run() {
    	getEiffelConfiguration();
    	createSender();
    	sendMsgPerSecond();
    	close();
    }

	public void getEiffelConfiguration() {
		String domainId = DataHandler.getAttribute("domainId").toString();
		String exchangeName = DataHandler.getAttribute("exchangeName").toString();
		String hostName = DataHandler.getAttribute("hostName").toString();
		this.config = new EiffelConfig(domainId, exchangeName, hostName);
		log.debug("Connected to domainId: "+domainId+" with exchange: "+exchangeName+" on host: "+hostName);
	}
	
	public void close() {
		this.sender.dispose();
		log.debug("Connected closed by API Operator");

	}
	
	private EiffelEvent eiffelEvents() {
		Random random = new Random();
		String jobInstance = "Server_Performance_Test";
        String jobExecutionId = "bcf820af-6bef-40a8-8b95-385bfb18e7f0";
        int jobExecutionNumber = 1306;
        
        EiffelEvent[] events = {EiffelJobQueuedEvent.Factory.create(jobInstance, jobExecutionId), 
        						EiffelJobStartedEvent.Factory.create(jobInstance, jobExecutionId, jobExecutionNumber), 
        						EiffelJobFinishedEvent.Factory.create(jobInstance, jobExecutionId, jobExecutionNumber, ResultCode.SUCCESS)};
        
        if(this.varyMessageType == true) {
        	return events[random.nextInt(events.length)];
        }
        else {
        	return events[2];
        }
	}
	
	private int getMessageRate() {
		Random random = new Random();
		if(this.varyMessageRate == true) {
			return random.nextInt(this.maxMessageRate);
		}
		else {
			return this.sustainedMessageRate;
		}
	}
	
	public void stop() {
		this.stopFlag = false;
	}
		
}
