package com.ericsson.eiffel.ve.web;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.internal.RESTEvent;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.plugins.VEMessageHandler;
import com.ericsson.eiffel.ve.web.dto.RESTEventImpl;

import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.socketio.SocketIOSessionOutbound;
import org.atmosphere.socketio.cpr.SocketIOAtmosphereHandler;
import org.atmosphere.socketio.transport.DisconnectReason;

import java.io.IOException;
import java.util.Set;


@AtmosphereHandlerService(path = "/socket.io", interceptors = {AtmosphereResourceLifecycleInterceptor.class})
public class VEDataHandler extends SocketIOAtmosphereHandler {

    private static final EiffelLog logger = new JavaLoggerEiffelLog(VEDataHandler.class);

    final Bootstrap bootstrap;
    
    public VEDataHandler() {
    	this(Bootstrap.getInstance());
    }
    
    public VEDataHandler(Bootstrap bootstrap) {
    	super();
    	this.bootstrap = bootstrap;
    }

    @Override
    public void onConnect(AtmosphereResource event, SocketIOSessionOutbound handler) throws IOException {
        String sessionId = handler.getSessionId();
        VEService veService = bootstrap.getVEService();
        veService.connect(sessionId, new AtmosphereConnection(handler));
        logger.info("Connected: " + sessionId);
    }

    @Override
    public void onDisconnect(AtmosphereResource event, SocketIOSessionOutbound handler, DisconnectReason reason) {
        String sessionId = handler.getSessionId();
        VEService veService = bootstrap.getVEService();
        veService.disconnect(sessionId);
        logger.info("Disconnected: " + sessionId);
    }

    @Override
    public void onMessage(AtmosphereResource event, SocketIOSessionOutbound handler, String message) {
        RESTEvent restEvent = new RESTEventImpl(message);
    	String eventURI = restEvent.getEventURI();
        if (eventURI != null) {
            String sessionId = handler.getSessionId();
            Set<VEMessageHandler> messageHandlers = bootstrap.getMessageHandlers(eventURI);
            for (VEMessageHandler messageHandler : messageHandlers) {
            	logger.debug("MESSAGEHANDLER: "+messageHandler.getClass().getName());
                messageHandler.handle(sessionId, restEvent);
            }
        } else {
            logger.warn("No topic handler for message: " + message);
        }
    }

    @Override
    public void destroy() {
    }
}
