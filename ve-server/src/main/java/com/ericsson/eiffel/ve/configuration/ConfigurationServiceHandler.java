package com.ericsson.eiffel.ve.configuration;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.plugins.VEMessageHandler;

public class ConfigurationServiceHandler implements VEMessageHandler {
    private static final String RESOURCE_NAME = "ve:rest/configurationhandler";

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public void handle(String sessionId, RESTEvent event) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public RESTEvent handleWithResponse(RESTEvent restEvent) throws Exception {
		ActorRef actor = Bootstrap.getInstance().getPluggedActor(ConfigurationServiceActorFactory.ACTOR_NAME);
		Timeout timeout = new Timeout(Duration.create(1, "seconds"));
		Future<Object> future = Patterns.ask(actor, restEvent, timeout);
		return (RESTEvent) Await.result(future, timeout.duration());
	}

}

