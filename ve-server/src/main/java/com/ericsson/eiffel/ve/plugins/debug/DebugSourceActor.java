package com.ericsson.eiffel.ve.plugins.debug;

import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import akka.actor.UntypedActor;

import com.ericsson.duraci.eiffelmessage.deserialization.Deserializer;
import com.ericsson.duraci.eiffelmessage.deserialization.exceptions.MessageDeserializationException;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.serialization.Serializer;
import com.ericsson.duraci.eiffelmessage.serialization.printing.exceptions.MessagePrintingException;
import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.actor.message.VEPublish;
import com.ericsson.eiffel.ve.actor.message.VESubscribe;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.api.internal.EventRepositoryAccessor;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.api.internal.VEConnection;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.application.EiffelMessageService;
import com.ericsson.eiffel.ve.application.dto.EiffelMessageWrapper;
import com.ericsson.eiffel.ve.application.routing.MessageRouter;
import com.ericsson.eiffel.ve.web.VEService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DebugSourceActor extends UntypedActor {

	final static EiffelLog logger = new JavaLoggerEiffelLog(DebugSourceActor.class);

	public static final TypeReference<List<EiffelMessageWrapper>> WRAPPER_LIST_TYPE =
			new TypeReference<List<EiffelMessageWrapper>>() {
	};

	private final String job;

	public DebugSourceActor(String job) {
		this.job = job;
	}

	@Override
	public void preStart() throws Exception {
		final Iterator<String> messages = getMessageIterator(loadDebugMessages(job));
		ActorSystem system = context().system();
		Scheduler scheduler = system.scheduler();
		scheduler.schedule(Duration.Zero(), Duration.create(1, TimeUnit.SECONDS), new Runnable() {
			@Override
			public void run() {
				String json = messages.next();
				self().tell(new VEPublish(json), self());
			}
		}, system.dispatcher());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof VEPublish) {
			publish((VEPublish) message);
		} else if (message instanceof VESubscribe) {
			subscribe((VESubscribe) message);
		} else {
			unhandled(message);
		}
	}

	private void subscribe(VESubscribe message) throws IOException {
		final VEService dashboardService = Bootstrap.getInstance().getVEService();
		final EiffelMessageService eiffelMessageService = Bootstrap.getInstance().getEiffelMessageService();
		final MessageRouter router = Bootstrap.getInstance().getMessageRouter();
		final String sessionId = message.getSessionId();
		final String topic = message.getEvent().getEventURI();

		List<EiffelMessageWrapper> wrappers = loadDebugMessages(job);
		for (EiffelMessageWrapper wrapper : wrappers) {
			EiffelMessage event = eiffelMessageService.unwrap(wrapper);
			router.getRouterResponse(event, topic)
			.serialize(new VEModel() {
				@Override
				public void consume(String json) {
					dashboardService.directPublish(sessionId, json);
				}

				@Override
				public void delete() {
				}

				@Override
				public String getModelName() {
					return "DebugModel";
				}

				@Override
				public void initSubscription(RESTEvent subscriptionEvent,
						VEConnection connection) {
				}

				@Override
				public void updateSubscription(RESTEvent subscriptionEvent) {
					
				}

				@Override
				public String query(RESTEvent queryEvent) {
					return null;
				}

				@Override
				public boolean matches(String json) {
					return false;
				}

				@Override
				public void setEventRepositoryAccessor(
						EventRepositoryAccessor eventRepositoryAccessor) {
				}
			});
		}
	}

	private void publish(VEPublish message) {
		VEService dashboardService = Bootstrap.getInstance().getVEService();
		String json = message.getJson();
		dashboardService.publish(json);
	}

	private static List<EiffelMessageWrapper> loadDebugMessages(String job) throws IOException {
		ObjectMapper objectMapper = Bootstrap.getInstance().getObjectMapper();
		return loadDebugMessages(job, objectMapper);
	}

	public static List<EiffelMessageWrapper> loadDebugMessages(String job,
			final ObjectMapper objectMapper)
					throws IOException {
		URL filesResource = Resources.getResource("debug/eiffel/" + job + ".json");
		String json = Resources.toString(filesResource, Charsets.UTF_8);

		JsonParser jp = new JsonParser();
		JsonArray jsonMessages = jp.parse(json).getAsJsonArray();

		List<EiffelMessageWrapper> list = new ArrayList<EiffelMessageWrapper>();
		EiffelMessageWrapper wrap;
		EiffelMessage message;
		String version;
		Deserializer deserializer = new Deserializer(logger);

		for(int i = 0; i < jsonMessages.size(); i++) {
			wrap = new EiffelMessageWrapper();
			try {
				message = deserializer.deserialize(jsonMessages.get(i).toString());
				version = getVersion(message);
				if(version != null) {
					Map<String, EiffelMessage> map = new HashMap<String, EiffelMessage>();
					map.put(version, message);
					wrap.setEiffelMessageVersions(map);
					list.add(wrap);
				}
			} catch (MessageDeserializationException e) {
				logger.error("Failed to deserialize Json '"+jsonMessages.get(i).toString() + "': " + e.getMessage());
			}
		}
		return list;
	}

	private static String getVersion(EiffelMessage message) {

		JsonParser jp = new JsonParser();

		try {
			String versionNeutralString = new Serializer(logger).compact(message).versionNeutralLatestOnly().print();
			JsonObject versionNeutralJson = jp.parse(versionNeutralString).getAsJsonObject();

			return versionNeutralJson.get("_messageVersion").getAsString();

		} catch (MessagePrintingException | JsonSyntaxException e) {
			logger.error("Failed to serialize EiffelMessage in version neutral form: " + e.getMessage());
		}

		return null;
	}

	private static Iterator<String> getMessageIterator(List<EiffelMessageWrapper> messageWrappers) {
		ObjectMapper objectMapper = Bootstrap.getInstance().getObjectMapper();
		EiffelMessageService eiffelMessageService = Bootstrap.getInstance().getEiffelMessageService();
		return getMessageIterator(messageWrappers, objectMapper, eiffelMessageService);
	}

	public static Iterator<String> getMessageIterator(List<EiffelMessageWrapper> messageWrappers,
			final ObjectMapper objectMapper,
			final EiffelMessageService eiffelMessageService) {
		SampleMessageIterator messageIterator =
				new SampleMessageIterator(messageWrappers, eiffelMessageService);
		return Iterators.transform(messageIterator, new Function<EiffelMessageWrapper, String>() {
			@Override
			public String apply(EiffelMessageWrapper messageWrapper) {
				JsonArray wrapper = new JsonArray();
				JsonObject eiffelMessage;

				Map<String, EiffelMessage> map = messageWrapper.getEiffelMessageVersions();
				Serializer serializer = new Serializer(logger);

				for(String key : map.keySet()) {
					eiffelMessage = new JsonObject();
					try {
						eiffelMessage.addProperty("key", key);
						eiffelMessage.addProperty("value", serializer.compact(map.get(key)).print());
						wrapper.add(eiffelMessage);
					} catch (MessagePrintingException e) {
						logger.error("Failed to serialize EiffelMessage: " + e.getMessage());
					}
				}

				return wrapper.toString();
			}
		});
	}

	private static class SampleMessageIterator implements Iterator<EiffelMessageWrapper> {

		@SuppressWarnings("unused")
		private final DateTimeFormatter iso8601;
		private final EiffelMessageService eiffelMessageService;
		private Collection<EiffelMessageWrapper> messageWrappers;
		private Iterator<EiffelMessageWrapper> messagesIterator;

		public SampleMessageIterator(Collection<EiffelMessageWrapper> messageWrappers,
				EiffelMessageService eiffelMessageService) {
			this.messageWrappers = messageWrappers;
			this.eiffelMessageService = eiffelMessageService;
			iso8601 = ISODateTimeFormat.dateTime().withZoneUTC();
			messagesIterator = setupIterator();
		}

		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public synchronized EiffelMessageWrapper next() {
			if (!messagesIterator.hasNext()) {
				messagesIterator = setupIterator();
			}
			return messagesIterator.next();
		}
		private Iterator<EiffelMessageWrapper> setupIterator() {
			// I don't know why they would generate a new UUID for the EiffelEvent so I've commented that out for now.

			Map<String, String> ids = new HashMap<>(messageWrappers.size());
			for (EiffelMessageWrapper messageWrapper : messageWrappers) {
				EiffelMessage message = eiffelMessageService.unwrap(messageWrapper);
				String oldId = message.getEventId().toString();
				String newId = newEventId();
				ids.put(oldId, newId);
			}
			Collection<EiffelMessageWrapper> newMessageWrappers = new ArrayList<>();
			for (EiffelMessageWrapper messageWrapper : messageWrappers) {
				// EiffelMessage message = eiffelMessageService.unwrap(messageWrapper);

				// String eventId = eiffelMessageService.getId(message);
				// eiffelMessageService.setId(message, ids.get(eventId));
				// String parentEventId = eiffelMessageService.getParentId(message);
				// eiffelMessageService.setParentId(message, ids.get(parentEventId));

				// message.setEventTime(iso8601.print(DateTime.now()));

				// EiffelMessageWrapper newMessageWrapper = eiffelMessageService.wrap(message);
				// newMessageWrappers.add(newMessageWrapper);
				newMessageWrappers.add(messageWrapper);
			}
			messageWrappers = newMessageWrappers;

			return messageWrappers.iterator();
		}

		private static String newEventId() {
			return UUID.randomUUID().toString();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
