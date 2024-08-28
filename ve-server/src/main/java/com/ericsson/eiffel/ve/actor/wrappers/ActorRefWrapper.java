package com.ericsson.eiffel.ve.actor.wrappers;

import akka.actor.ActorRef;

import com.ericsson.eiffel.ve.actor.message.VEMessage;

public class ActorRefWrapper {
	private final ActorRef actorRef;
	
	public ActorRefWrapper(final ActorRef veMasterActor) {
		this.actorRef = veMasterActor;
	}
	
	public void tell(VEMessage message) {
		this.actorRef.tell(message, ActorRef.noSender());
	}

}
