package com.ericsson.eiffel.ve.actor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.actor.message.VEDisconnect;
import com.ericsson.eiffel.ve.actor.message.VEPublish;
import com.ericsson.eiffel.ve.actor.message.VESubscribe;
import com.ericsson.eiffel.ve.actor.message.VEUnsubscribe;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.api.internal.VEConnection;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.application.routing.MessageRouter;

public class VEActor extends UntypedActor {
	private static final EiffelLog logger = new JavaLoggerEiffelLog(VEActor.class);

	private final Map<String, VEModel> consumers;
	private final VEConnection connection;
	private final MessageRouter router;

	public VEActor(VEConnection connection, MessageRouter router) {
		this.consumers = new HashMap<String, VEModel>();
		this.connection = connection;
		this.router = router;
	}

	public static Props makeProps(VEConnection connection, MessageRouter router) {
		return Props.create(VEActor.class, connection, router);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof VESubscribe) {
			subscribe((VESubscribe) message);
		} else if (message instanceof VEUnsubscribe) {
			unsubscribe((VEUnsubscribe) message);
		} else if (message instanceof VEPublish) {
			publish((VEPublish) message);
		} else if (message instanceof VEDisconnect) {
			disconnect((VEDisconnect) message);
		} else {
			unhandled(message);
		}
	}

	private void subscribe(VESubscribe message) {
		String eventURI = message.getEvent().getEventURI();
		String id = extractUUIDFromURI(eventURI);

		if(id != null) {
			if(message.getEvent().getEventBody().has("model")) {
				String model = message.getEvent().getEventBody().get("model").getAsString();
				logger.debug("Subscribing for model: "+model);

				if(!consumers.containsKey(id)) {
					consumers.put(id, createConsumerForModel(message.getEvent(), model));
				}
				else {
					consumers.get(id).updateSubscription(message.getEvent());
				}

				router.subscribe(id, consumers.get(id));
			}
			else {
				logger.error("No model defined in subscription");
			}
		}
		else {
			logger.error("No id found for subscription request.");
		}
	}

	private void unsubscribe(VEUnsubscribe message) {
		String eventURI = message.getEvent().getEventURI();
		String id = extractUUIDFromURI(eventURI);
		router.unsubscribe(id, consumers.get(id));
		consumers.get(id).delete();
		consumers.remove(id);
	}

	private void publish(VEPublish message) {
		String json = message.getJson();
		for(VEModel consumer : consumers.values())
			consumer.consume(json);
	}

	private void disconnect(VEDisconnect message) {
		for(VEModel consumer : consumers.values()) {
			router.unsubscribe(consumer);
			consumer.delete();
		}

		consumers.clear();

		self().tell(PoisonPill.getInstance(), self());
	}

	private String extractUUIDFromURI(String eventURI) {
		String id = null;

		if(eventURI.endsWith("/"))
			eventURI = eventURI.substring(0, eventURI.length()-1);

		try {
			UUID uuid = UUID.fromString(eventURI.substring(eventURI.lastIndexOf('/')+1));
			id = uuid.toString();
		} catch(IllegalArgumentException e) {
			logger.error("Found no UUID in URI: '"+eventURI+"'.");
		}

		return id;
	}

	// TODO: Consider how errors should be handled in this method
	private VEModel createConsumerForModel(RESTEvent subscriptionEvent, String model) {
		VEModel consumer = null;
		Class<? extends VEModel> dsClass;
		try {
			dsClass = Bootstrap.getInstance().getModel(model);
			final Constructor<?> dsConst = dsClass.getDeclaredConstructor();
			consumer = (VEModel)dsConst.newInstance();
			consumer.setEventRepositoryAccessor(Bootstrap.getInstance().getEventRepositoryAccessor());
			consumer.initSubscription(subscriptionEvent, connection);
		} catch (NoSuchMethodException e) {
			logger.error(e.getMessage());
		} catch (SecurityException e) {
			logger.error(e.getMessage());
		} catch (InstantiationException e) {
			logger.error(e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage());
		}
		return consumer;
	}
}
