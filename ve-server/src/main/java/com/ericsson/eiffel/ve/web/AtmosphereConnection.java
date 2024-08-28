package com.ericsson.eiffel.ve.web;

import com.ericsson.eiffel.ve.api.internal.VEConnection;
import com.google.common.base.Throwables;

import org.atmosphere.socketio.SocketIOException;
import org.atmosphere.socketio.SocketIOPacket;
import org.atmosphere.socketio.SocketIOSessionOutbound;
import org.atmosphere.socketio.transport.SocketIOPacketImpl;

/**
 *
 */
public class AtmosphereConnection implements VEConnection {

    private SocketIOSessionOutbound session;

    public AtmosphereConnection(SocketIOSessionOutbound session) {
        this.session = session;
    }

	@Override
	public void send(String name, String message) {
        try {
            SocketIOPacket packet = new SocketIOPacketImpl(
                    SocketIOPacketImpl.PacketType.EVENT,
                    "{\"name\":\"" + name + "\", \"args\":[" + message + "]}"
            );
            session.sendMessage(packet);
        } catch (SocketIOException e) {
            throw Throwables.propagate(e);
        }
	}

}
