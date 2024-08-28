package com.ericsson.eiffel.ve.plugins.debug;

import akka.actor.Props;

import com.ericsson.eiffel.ve.infrastructure.config.Settings;
import com.ericsson.eiffel.ve.plugins.DashboardActorFactory;

public class DebugActorFactory implements DashboardActorFactory {

    public static final String ACTOR_NAME = "debug-source";

    @Override
    public Props makeProps(Settings settings) {
        return Props.create(DebugSourceActor.class, settings.getString("debug.job"));
    }

    @Override
    public String getName() {
        return ACTOR_NAME;
    }
}
