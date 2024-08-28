package com.ericsson.eiffel.ve;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.application.Dashboard;
import com.ericsson.eiffel.ve.application.DashboardConfiguration;
import com.ericsson.eiffel.ve.configuration.ConfigurationServiceActorFactory;
import com.ericsson.eiffel.ve.configuration.ConfigurationServiceHandler;
import com.ericsson.eiffel.ve.infrastructure.config.Settings;
import com.ericsson.eiffel.ve.infrastructure.config.SettingsProvider;
import com.ericsson.eiffel.ve.plugins.amqp.AmqpActorFactory;
import com.ericsson.eiffel.ve.plugins.er.EventRepositoryActorFactory;
import com.ericsson.eiffel.ve.plugins.er.EventRepositoryHandler;
/**
 *
 */
public class Application {
	private static final EiffelLog logger = new JavaLoggerEiffelLog(Application.class);

	public static void main(String[] args) throws Exception {
    	if (args.length != 1) {
    		System.out.println("Usage: ve-server-<VERSION>.jar <path to settings file>");
    		System.exit(0);
    	}
		final String settingsFilePath = args[0];  
    	Settings settings = new SettingsProvider(settingsFilePath).loadSettings();

		loadExternalJars(settings);

		boolean barebone = Boolean.parseBoolean(System.getProperty("app.barebone"));
		DashboardConfiguration.Builder configBuilder = new DashboardConfiguration.Builder();
		if (!barebone) {
			if (settings.getString("amqp.host") != null){
				configBuilder
				.actor(new AmqpActorFactory());
			} else {
				logger.info("No amqp configuration present!");
			}
			if (settings.getString("eventRepository.uri") != null){
				configBuilder
				.actor(new EventRepositoryActorFactory())
				.messageHandler(new EventRepositoryHandler());
			} else {
				logger.info("No er configuration present!");
			}
			if (settings.getString("mongodb.hostname") != null){
				configBuilder
				.actor(new ConfigurationServiceActorFactory())
				.messageHandler(new ConfigurationServiceHandler());
			} else {
				logger.info("No MongoDB configuration present!");
			}
		}

		DashboardConfiguration config = configBuilder.build();
		Dashboard dashboard = new Dashboard(config);
		dashboard.start(settings);
	}

	private static void loadExternalJars(Settings settings){
		String jarPaths = settings.getString("plugin.jars");

		if (jarPaths != null) {
			logger.info("Loading plugins from JAR(s): " + jarPaths);
			ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

			// Add the jars to the classpath
			// Chain the current thread classloader
			ArrayList<URL> urlsList = new ArrayList<URL>();
			for (String jar : jarPaths.split(",")) {
				try {
					urlsList.add(new File(jar).toURI().toURL());
				} catch (MalformedURLException e) {
					logger.error("Error loading external JARs. Could not convert '"+jar+"' to an URL so it is skipped. Cause: " + e.getMessage());
				}
			}
			URL[] urls = new URL[urlsList.size()];
			urlsList.toArray(urls);
			try {
				URLClassLoader urlClassLoader = new URLClassLoader(urls
						, currentThreadClassLoader);

				// Replace the thread classloader - assumes
				// you have permissions to do so
				Thread.currentThread().setContextClassLoader(urlClassLoader);

			} catch (SecurityException e){
				logger.error("Error loading external JAR due to: " + e.getMessage());
			}
		}
	}
}
