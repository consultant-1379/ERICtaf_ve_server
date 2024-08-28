package com.ericsson.eiffel.ve.application.routing;

import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.deserialization.Deserializer;
import com.ericsson.duraci.eiffelmessage.serialization.Serializer;
import com.ericsson.duraci.eiffelmessage.serialization.printing.exceptions.MessagePrintingException;
import com.ericsson.duraci.eiffelmessage.deserialization.exceptions.MessageDeserializationException;
import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.application.EiffelMessageService;
import com.ericsson.eiffel.ve.web.ServerResponseSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EiffelMessageRouter implements MessageRouter {

    public static final String SENDER_NAME = "router";

    private static final EiffelLog logger = new JavaLoggerEiffelLog(EiffelMessageRouter.class);


    @SuppressWarnings("unused")
	private final ObjectMapper objectMapper;
    private final EiffelMessageService eiffelMessageService;

    private final SetMultimap<JsonRoute, VEModel> routeConsumers;
    private final SetMultimap<VEModel, JsonRoute> consumerRoutes;
    
    private final List<VEModel> consumers;

    public EiffelMessageRouter(ObjectMapper objectMapper, EiffelMessageService eiffelMessageService) {
        this.objectMapper = objectMapper;
        this.eiffelMessageService = eiffelMessageService;
        this.routeConsumers = Multimaps.synchronizedSetMultimap(HashMultimap.<JsonRoute, VEModel>create());
        this.consumerRoutes = HashMultimap.create();
        this.consumers = Collections.synchronizedList(new ArrayList<VEModel>());
    }

    @Override
    public void subscribe(String id, VEModel routable) {
    	consumers.add(routable);
    }

    @Override
    public void unsubscribe(String id, VEModel routable) {
    	consumers.remove(routable);
    }

    @Override
    public void unsubscribe(VEModel routable) {
    	consumers.remove(routable);
    }

    @Override
    public void publish(String json) {
        EiffelMessage message;
		try {
			message = new Deserializer(logger).deserialize(json);
			logger.info("Publishing message...");
			checkForConsumers(message);
		} catch (MessageDeserializationException e) {
			logger.warn("Failed to parse Eiffel message: " + e.getMessage());
		}
    }

    protected void checkForConsumers(EiffelMessage message) {
        String json = null;
        try {
        	json = new Serializer(logger).compact(message).versionNeutralLatestOnly().print();
        } catch (MessagePrintingException e) {
            logger.warn("Failed to emit Eiffel message: " + e.getMessage());
		}
        
        synchronized (consumers) {
			for(VEModel mc : consumers) {
				if(mc.matches(json))
					mc.consume(json);
			}
		}
    }

    @Override
    public ServerResponseSerializer getRouterResponse(EiffelMessage eiffelMessage, String topic) {
        return ServerResponseSerializer.create(SENDER_NAME)
                .message(eiffelMessage)
                .data("topic", topic);
    }

	@Override
	public void historicaldata(String filters, VEModel routable) {
		JsonRoute route = JsonRoute.parseHistoricalData(filters);
		logger.debug("created JsonRoute for HistoricalData: " + route.toString());
        if (route != null) {
            routeConsumers.put(route, routable);
            consumerRoutes.put(routable, route);
        }
	}

}
