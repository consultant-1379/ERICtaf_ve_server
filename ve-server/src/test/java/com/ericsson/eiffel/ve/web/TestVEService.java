package com.ericsson.eiffel.ve.web;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.actor.message.VEConnect;
import com.ericsson.eiffel.ve.actor.message.VEDirectPublish;
import com.ericsson.eiffel.ve.actor.message.VEDisconnect;
import com.ericsson.eiffel.ve.actor.message.VEHistoricalData;
import com.ericsson.eiffel.ve.actor.message.VEPublish;
import com.ericsson.eiffel.ve.actor.message.VESubscribe;
import com.ericsson.eiffel.ve.actor.message.VEUnsubscribe;
import com.ericsson.eiffel.ve.actor.wrappers.ActorRefWrapper;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.web.dto.RESTEventImpl;

public class TestVEService {

	private VEService unitUnderTest;
	private ActorRefWrapper actorRefWrapper;
	private final String sessionId = "sessionId";
	private final String json = "{}";
	private AtmosphereConnection connection;
	private RESTEvent event;
	
	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		actorRefWrapper = mock(ActorRefWrapper.class);
		connection = mock(AtmosphereConnection.class);
		event = mock(RESTEventImpl.class);
		unitUnderTest = new VEService(actorRefWrapper);
	}

	@Test
	public void testConnect() {
		unitUnderTest.connect(sessionId, connection);
		verify(actorRefWrapper).tell(any(VEConnect.class));
	}

	@Test
	public void testDisconnect() {
		unitUnderTest.disconnect(sessionId);
		verify(actorRefWrapper).tell(any(VEDisconnect.class));
	}

	@Test
	public void testSubscribe() {
		unitUnderTest.subscribe(sessionId, event);
		verify(actorRefWrapper).tell(any(VESubscribe.class));
	}

	@Test
	public void testUnsubscribe() {
		unitUnderTest.unsubscribe(sessionId, event);
		verify(actorRefWrapper).tell(any(VEUnsubscribe.class));
	}

	@Test
	public void testPublish() {
		unitUnderTest.publish(json);
		verify(actorRefWrapper).tell(any(VEPublish.class));
	}

	@Test
	public void testDirectPublish() {
		unitUnderTest.directPublish(sessionId, json);
		verify(actorRefWrapper).tell(any(VEDirectPublish.class));
	}
}
