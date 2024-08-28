package com.ericsson.eiffel.ve.web;

import akka.actor.ActorSystem;

import com.ericsson.eiffel.ve.actor.VEMasterActor;
import com.ericsson.eiffel.ve.actor.message.VEConnect;
import com.ericsson.eiffel.ve.actor.message.VEDirectPublish;
import com.ericsson.eiffel.ve.actor.message.VEDisconnect;
import com.ericsson.eiffel.ve.actor.message.VEHistoricalData;
import com.ericsson.eiffel.ve.actor.message.VEPublish;
import com.ericsson.eiffel.ve.actor.message.VESubscribe;
import com.ericsson.eiffel.ve.actor.message.VEUnsubscribe;
import com.ericsson.eiffel.ve.actor.wrappers.ActorRefWrapper;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.application.routing.MessageRouter;

public class VEService {

    private final ActorRefWrapper actorRefWrapper;

    public VEService(ActorRefWrapper actorRefWrapper) {
    	this.actorRefWrapper = actorRefWrapper;
    }

    public VEService(ActorSystem actorSystem, MessageRouter jsonRouter) {
        actorRefWrapper = new ActorRefWrapper(actorSystem.actorOf(VEMasterActor.makeProps(jsonRouter)));
    }

    public void connect(String sessionId, AtmosphereConnection connection) {
        actorRefWrapper.tell(new VEConnect(sessionId, connection));
    }

    public void disconnect(String sessionId) {
        actorRefWrapper.tell(new VEDisconnect(sessionId));
    }

    public void subscribe(String sessionId, RESTEvent event) {
        actorRefWrapper.tell(new VESubscribe(sessionId, event));
    }

    public void unsubscribe(String sessionId, RESTEvent event) {
        actorRefWrapper.tell(new VEUnsubscribe(sessionId, event));
    }

    public void publish(String json) {
        actorRefWrapper.tell(new VEPublish(json));
    }

    public void directPublish(String sessionId, String json) {
        actorRefWrapper.tell(new VEDirectPublish(sessionId, json));
    }
}
