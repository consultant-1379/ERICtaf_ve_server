/*
 * Copyright 2013 Sebastien Dionne
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.socketio.cpr;

import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.socketio.SocketIOException;
import org.atmosphere.socketio.SocketIOSession;
import org.atmosphere.socketio.SocketIOWebSocketSessionWrapper;
import org.atmosphere.socketio.transport.SocketIOPacketImpl;
import org.atmosphere.socketio.transport.SocketIOPacketImpl.PacketType;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;

import java.io.IOException;
import java.util.List;

/**
 * @author Sebastien Dionne  : sebastien.dionne@gmail.com
 * @author Jeanfrancois Arcand
 */
public class SocketIOWebSocketEventListener extends WebSocketEventListenerAdapter {

    private static final EiffelLog logger = new JavaLoggerEiffelLog(SocketIOWebSocketEventListener.class);

    private SocketIOWebSocketSessionWrapper sessionWrapper = null;

    public void setSessionWrapper(SocketIOWebSocketSessionWrapper sessionWrapper) {
        this.sessionWrapper = sessionWrapper;
    }

    @Override
    public void onMessage(WebSocketEvent event) {
        logger.debug("calling from " + this.getClass().getName() + " : " + "onMessage");

        if (!sessionWrapper.isInitiated()) {
            if ("OPEN".equals(event.message())) {
                try {
                    sessionWrapper.getSession().onConnect(sessionWrapper.getSession().getAtmosphereResourceImpl(), sessionWrapper);
                    sessionWrapper.initiated(true);
                } catch (Exception e) {
                    sessionWrapper.webSocket().close();
                    sessionWrapper.getSession().onShutdown();
                }
            } else {
                sessionWrapper.webSocket().close();
                sessionWrapper.getSession().onShutdown();
            }
        } else {
            List<SocketIOPacketImpl> messages = null;
            try {
                messages = SocketIOPacketImpl.parse(event.message().toString());
            } catch (SocketIOException e) {
                logger.warn("Failed to parse event message due to: " + e.getMessage());
            }

            if (messages != null && !messages.isEmpty()) {
                SocketIOSession session = sessionWrapper.getSession();
                for (SocketIOPacketImpl msg : messages) {
                    session.onMessage(session.getAtmosphereResourceImpl(), session.getTransportHandler(), msg.getData());
                }
            }

        }
    }

    @Override
    public void onConnect(WebSocketEvent event) {
        logger.debug("calling from " + this.getClass().getName() + " : " + "onConnect");

        sessionWrapper.setWebSocket(event.webSocket());
        try {
            event.webSocket().write(new SocketIOPacketImpl(PacketType.CONNECT).toString());
        } catch (IOException e) {
            e.printStackTrace();
            sessionWrapper.getSession().onShutdown();
        }

        try {
            sessionWrapper.getSession().setAtmosphereResourceImpl((AtmosphereResourceImpl) event.webSocket().resource());
            sessionWrapper.getSession().onConnect(sessionWrapper.getSession().getAtmosphereResourceImpl(), sessionWrapper);
            sessionWrapper.initiated(true);
        } catch (Exception e) {
            e.printStackTrace();
            sessionWrapper.getSession().onShutdown();
        }

    }

    @Override
    public void onClose(WebSocketEvent event) {
        logger.debug("calling from " + this.getClass().getName() + " : " + "onClose event = " + event);

        SocketIOSession session = sessionWrapper.getSession();
        session.startClose(); // FIXME Force socket closing callbacks from server
        session.onClose("server");
    }

}
