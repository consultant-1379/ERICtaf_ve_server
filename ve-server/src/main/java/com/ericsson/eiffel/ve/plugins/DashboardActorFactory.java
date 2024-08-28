package com.ericsson.eiffel.ve.plugins;

import akka.actor.Props;

import com.ericsson.eiffel.ve.infrastructure.config.Settings;

public interface DashboardActorFactory {
    Props makeProps(Settings settings);

    String getName();
}
