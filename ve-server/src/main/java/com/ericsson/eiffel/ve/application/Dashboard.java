package com.ericsson.eiffel.ve.application;

import com.ericsson.eiffel.ve.infrastructure.config.Settings;

public class Dashboard {

    private final DashboardConfiguration configuration;

    public Dashboard(DashboardConfiguration configuration) {
        this.configuration = configuration;
    }

    public void start(Settings settings) {
        final Bootstrap bootstrap = Bootstrap.getInstance();
        bootstrap.init(configuration, settings);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                bootstrap.shutdown();
            }
        });
    }
}
