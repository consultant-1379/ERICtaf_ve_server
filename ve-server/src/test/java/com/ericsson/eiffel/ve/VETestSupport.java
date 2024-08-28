package com.ericsson.eiffel.ve;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VETestSupport {
	// To prevent the slim chance that the logger is garbage collected before the test is completed
	private static Logger topLogger = null;

	public static void setupLogging(){
		Level level = Level.FINE;

		//get the top Logger:
		if (topLogger == null){
			topLogger = Logger.getLogger("");
		}
		//set the top logger level
		topLogger.setLevel(level);

		// Handler for console (reuse it if it already exists)
		Handler consoleHandler = null;
		//see if there is already a console handler
		for (Handler handler : topLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				//found the console handler
				consoleHandler = handler;
				break;
			}
		}

		if (consoleHandler == null) {
			//there was no console handler found, create a new one
			consoleHandler = new ConsoleHandler();
			topLogger.addHandler(consoleHandler);
		}
		//set the console handler level:
		consoleHandler.setLevel(level);
	}
}