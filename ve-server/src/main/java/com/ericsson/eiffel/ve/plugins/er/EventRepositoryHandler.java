package com.ericsson.eiffel.ve.plugins.er;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.plugins.VEMessageHandler;

public class EventRepositoryHandler implements VEMessageHandler {

    private final String RESOURCE_NAME = "ve:historicaldata/queryhandler";
    
	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public void handle(String sessionId, RESTEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public RESTEvent handleWithResponse(RESTEvent event) throws Exception {
		ActorRef actor = Bootstrap.getInstance().getPluggedActor(EventRepositoryActorFactory.ACTOR_NAME);
		Timeout timeout = new Timeout(Duration.create(1,  "seconds"));//TODO Add more time?
		Future<Object> future = Patterns.ask(actor, event, timeout);
		return (RESTEvent) Await.result(future, timeout.duration());
	}

}
