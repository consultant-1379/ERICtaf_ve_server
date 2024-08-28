package com.ericsson.eiffel.ve.api.internal;

/**
 * Interface for all classes implementing a WebSocket connection used for sending data to a
 * Visualization Engine client. No data is received through this interface.
 * 
 * @author xdanols
 *
 */
public interface VEConnection {
	
	/**
	 * The send method is used for sending a message through socket.io to a VE client. The
	 * name variable defines what will be the name or type of the message (this should for
	 * most cases in VE models be 'update'), and the message variable is the actual message
	 * as a String.
	 * @param name Name of the message
	 * @param message The actual message data to be sent
	 */
    void send(String name, String message);
}
