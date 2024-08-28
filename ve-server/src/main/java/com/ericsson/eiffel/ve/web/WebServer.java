package com.ericsson.eiffel.ve.web;

import com.ericsson.eiffel.ve.infrastructure.config.Settings;

import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;
import org.atmosphere.socketio.cpr.SocketIOAtmosphereInterceptor;

/**
 *
 */
public class WebServer {

    private Nettosphere server;
    private Settings settings;

    public WebServer(Settings settings) {
        this.settings = settings;
    }

    public void start() {
        server = new Nettosphere.Builder().config(
                new Config.Builder()
                		.initParam(SocketIOAtmosphereInterceptor.SOCKETIO_TRANSPORT, "websocket,xhr-polling,jsonp-polling")
                        .resource(VEDataHandler.class)
                        .mappingPath("/socket.io")
                        .interceptor(new SocketIOAtmosphereInterceptor())
                        .resource(settings.getString("netty.webdir"))
                        .host(settings.getString("netty.hostname"))
                        .port(settings.getInteger("netty.port"))
                        .resource("/socket.io/historicaldata/queryhandler", VEHistoricalDataHandler.class)
                        .resource("/socket.io/configuration/dashboards", VEConfigurationServiceHandler.class)
                        .resource("/socket.io/configuration/views", VEConfigurationServiceHandler.class)
                        .resource("/socket.io/configuration/typeoptions", VEConfigurationServiceHandler.class)
                        .build())
                .build();

        server.start();
    }

    public void stop() {
        server.stop();
    }

}
