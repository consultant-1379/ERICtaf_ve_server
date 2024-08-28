package com.ericsson.eiffel.ve.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.ericsson.eiffel.ve.actor.message.VEConnect;
import com.ericsson.eiffel.ve.actor.message.VEDirectPublish;
import com.ericsson.eiffel.ve.actor.message.VEDisconnect;
import com.ericsson.eiffel.ve.actor.message.VEMessage;
import com.ericsson.eiffel.ve.actor.message.VEPublish;
import com.ericsson.eiffel.ve.actor.message.VESubscribe;
import com.ericsson.eiffel.ve.actor.message.VEUnsubscribe;
import com.ericsson.eiffel.ve.api.internal.VEConnection;
import com.ericsson.eiffel.ve.application.routing.MessageRouter;

import java.util.HashMap;
import java.util.Map;

public class VEMasterActor extends UntypedActor {

    private final Map<String, ActorRef> dashboards;
    private final MessageRouter router;

    public VEMasterActor(Map<String, ActorRef> dashboards, MessageRouter router) {
        this.dashboards = dashboards;
        this.router = router;
    }

    public static Props makeProps(MessageRouter router) {
        return Props.create(VEMasterActor.class, new HashMap<>(), router);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof VEConnect) {
            connect((VEConnect) message);
        } else if (message instanceof VEDisconnect) {
            disconnect((VEDisconnect) message);
        } else if (message instanceof VESubscribe) {
            propagate((VESubscribe) message);
        } else if (message instanceof VEUnsubscribe) {
            propagate((VEUnsubscribe) message);
        } else if (message instanceof VEPublish) {
            publish((VEPublish) message);
        } else if (message instanceof VEDirectPublish) {
            directPublish((VEDirectPublish) message);
        } else {
            unhandled(message);
        }
    }

    private void propagate(VEMessage message) {
        String sessionId = message.getSessionId();
        ActorRef dashboard = dashboards.get(sessionId);
        if (dashboard != null) {
            dashboard.tell(message, self());
        }
    }

    private void connect(VEConnect message) {
        VEConnection connection = message.getConnection();
        String sessionId = message.getSessionId();
        ActorRef actor = context().actorOf(VEActor.makeProps(connection, router));
        dashboards.put(sessionId, actor);
    }

    private void disconnect(VEDisconnect message) {
        String sessionId = message.getSessionId();
        ActorRef dashboard = dashboards.get(sessionId);
        if (dashboard != null) {
            dashboard.tell(message, self());
            dashboards.remove(sessionId);
        }
    }

    private void publish(VEPublish message) {
        String json = message.getJson();
        router.publish(json);
    }

    private void directPublish(VEDirectPublish message) {
        String sessionId = message.getSessionId();
        String json = message.getJson();
        ActorRef dashboard = dashboards.get(sessionId);
        dashboard.tell(new VEPublish(json), self());
    }
}
