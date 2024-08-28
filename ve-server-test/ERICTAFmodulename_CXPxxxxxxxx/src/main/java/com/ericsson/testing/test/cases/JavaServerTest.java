package com.ericsson.testing.test.cases;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TestExecutionHelper;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.testing.test.operators.EventEmulatorOperatorAPI;
import com.ericsson.testing.test.operators.WebSocketOperator;
import com.ericsson.testing.test.operators.WebSocketOperatorAPI;





import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.BaseSUT;
import se.ericsson.jcat.fw.EventThreadPool;

import javax.inject.Inject;

@Test(enabled=false)
public class JavaServerTest extends TorTestCaseHelper implements TestCase {

    @Inject
    OperatorRegistry<WebSocketOperator> operatorRegistry;
    
    EventEmulatorOperatorAPI eventEmulator = new EventEmulatorOperatorAPI();
    Thread e = new Thread(this.eventEmulator); 
   
    
    @BeforeSuite
    public void sendMessages() {
    	e.start(); 	
    }
    
    /**
     * Test to create websocket connection to an active server and subscribe to events. Shutdown of the socket connection after a set period of time
     * @param hostname
     * @param port
     * @param subscription
     * @param timeout
     * @throws InterruptedException 
     */
    @Context(context = {Context.API})
    @DataDriven(name = "ERICTAFmodulename_CXPxxxxxxxx.javaServerTest")
    @Test(enabled=false)
    public void sendEventsForServerToFind(@Input("host") String hostname, @Input("port") int port, @Input("timeout") long timeout,
    		@Input("topic") String topic, @Input("model") String model, @Input("uuid") String uuid) throws InterruptedException {
    	WebSocketOperatorAPI client = new WebSocketOperatorAPI(hostname,port,topic, model, uuid);
    	Thread con = new Thread(client);
    	con.setDaemon(true);
    	con.start();
    	Thread.sleep(timeout);
    	assert(client.isConnected());   		
        con.interrupt();
    }
    
    @AfterSuite
    public void finish() {     	
    	e.interrupt();
    }
    
}
