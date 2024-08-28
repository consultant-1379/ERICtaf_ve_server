package com.ericsson.eiffel.ve.plugins.debug;

import akka.actor.ActorRef;

import com.ericsson.eiffel.ve.actor.message.VESubscribe;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.plugins.VEMessageHandler;

public class DebugSourceHandler implements VEMessageHandler {

	@Override
	public String getResourceName() {
		return "ve:debug/service";
	}

	@Override
	public void handle(String sessionId, RESTEvent event) {
        ActorRef actor = Bootstrap.getInstance().getPluggedActor(DebugActorFactory.ACTOR_NAME);
        actor.tell(new VESubscribe(sessionId, event), ActorRef.noSender());
	}

	@Override
	public RESTEvent handleWithResponse(RESTEvent event) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
