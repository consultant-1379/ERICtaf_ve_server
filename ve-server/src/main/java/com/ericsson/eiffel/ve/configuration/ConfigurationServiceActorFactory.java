package com.ericsson.eiffel.ve.configuration;

import akka.actor.Props;

import com.ericsson.eiffel.ve.infrastructure.config.Settings;
import com.ericsson.eiffel.ve.plugins.DashboardActorFactory;

public class ConfigurationServiceActorFactory implements DashboardActorFactory {
    public static final String ACTOR_NAME = "configuration-service";

	@Override
	public Props makeProps(Settings settings) {
        return Props.create(ConfigurationServiceActor.class,
                settings.getString("mongodb.hostname"));
	}

	@Override
	public String getName() {
        return ACTOR_NAME;
	}

}
