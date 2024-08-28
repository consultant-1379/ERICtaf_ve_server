package com.ericsson.eiffel.ve.plugins.er;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.web.dto.RESTEventImpl;
import com.google.gson.JsonObject;

public class EventRepositoryActor extends UntypedActor {

    private static final EiffelLog logger = new JavaLoggerEiffelLog(EventRepositoryActor.class);

    public EventRepositoryActor() {
    	logger.debug("Creating EventRepositoryActor");
    }

    @Override
    public void onReceive(Object message) throws Exception {
    	logger.debug("Received message: " + message.toString());
    	if (message instanceof RESTEventImpl) {
            handle((RESTEvent) message);
        } else {
            unhandled(message);
        }
    }
    
    private void handle(RESTEvent message) {
    	logger.debug("Handling message: " + message.toString());
    	JsonObject eventBody = message.getEventBody();
    	String modelName = eventBody.get("model").toString().replace("\"", ""); //Remove the " from the name
    	
		VEModel model = (VEModel) Bootstrap.getInstance().getRESTInstanceForModel(modelName);
		logger.debug("Querying the model " + modelName + ".");
		String modelResponse = model.query(message);
		logger.debug("Model response: " + modelResponse);
		JsonObject restEvent = new JsonObject();
		restEvent.addProperty("eventBody", modelResponse);
		
		getSender().tell(new RESTEventImpl(modelResponse), ActorRef.noSender());
    }

}
