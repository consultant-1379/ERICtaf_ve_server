package com.ericsson.eiffel.ve.plugins.er;

import akka.actor.Props;

import com.ericsson.eiffel.ve.infrastructure.config.Settings;
import com.ericsson.eiffel.ve.plugins.DashboardActorFactory;

public class EventRepositoryActorFactory implements DashboardActorFactory {

    public static final String ACTOR_NAME = "er-source";

    @Override
    public Props makeProps(Settings settings) {
        return Props.create(EventRepositoryActor.class);
    }

    @Override
    public String getName() {
        return ACTOR_NAME;
    }
}
