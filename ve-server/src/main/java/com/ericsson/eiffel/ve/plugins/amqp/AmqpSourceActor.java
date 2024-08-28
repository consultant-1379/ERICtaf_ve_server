package com.ericsson.eiffel.ve.plugins.amqp;

import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.web.VEService;

/**
 *
 */
public class AmqpSourceActor extends UntypedConsumerActor {

    private static final EiffelLog logger = new JavaLoggerEiffelLog(AmqpSourceActor.class);

    private final String host;
    private final Integer port;
    private final String exchangeName;
    private final String username;
    private final String password;
    private final String routingKey;
    private final String componentName;

    public AmqpSourceActor(String host,
                           Integer port,
                           String exchangeName,
                           String username,
                           String password,
                           String routingKey,
                           String componentName) {
        this.host = host;
        this.port = port;
        this.exchangeName = exchangeName;
        this.username = username;
        this.password = password;
        this.routingKey = routingKey;
        this.componentName = componentName;
    }

    @Override
    public String getEndpointUri() {
    	String queueName = routingKey + "." + componentName + "." + "AmqpSourceActor" + "." + "transient";

        return String.format("rabbitmq://%s:%s/%s?username=%s&password=%s&durable=false&autoDelete=false&routingKey=%s&queue=%s",
                host, port, exchangeName, username, password, routingKey, queueName);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        logger.info("AMQP event received");
        if (message instanceof CamelMessage) {
            CamelMessage camelMessage = (CamelMessage) message;
            logger.info("Handle message");
            handle(camelMessage);
        } else {
        	logger.info("Unhandled message");
            unhandled(message);
        }
    }

	private void handle(CamelMessage message) {
        VEService dashboardService = Bootstrap.getInstance().getVEService();
        String jsonMessage = message.getBodyAs(String.class, getCamelContext());
        logger.debug("Handle JSON message: " + jsonMessage);
        dashboardService.publish(jsonMessage);
    }

}
