package com.ericsson.eiffel.ve.application.routing;

import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.web.ServerResponseSerializer;

/**
 *
 */
public interface MessageRouter {

    void subscribe(String id, VEModel routable);

    void unsubscribe(String id, VEModel routable);

    void unsubscribe(VEModel routable);
    
    void historicaldata(String filters, VEModel routable);

    void publish(String json);

    ServerResponseSerializer getRouterResponse(EiffelMessage eiffelMessage, String topic);

}
