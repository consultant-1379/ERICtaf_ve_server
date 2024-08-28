package com.ericsson.eiffel.ve.plugins.amqp;

import akka.actor.Props;

import com.ericsson.eiffel.ve.infrastructure.config.Settings;
import com.ericsson.eiffel.ve.plugins.DashboardActorFactory;

public class AmqpActorFactory implements DashboardActorFactory {

    public static final String ACTOR_NAME = "amqp-source";

    @Override
    public Props makeProps(Settings settings) {
        return Props.create(AmqpSourceActor.class,
                settings.getString("amqp.host"),
                settings.getInteger("amqp.port"),
                settings.getString("amqp.exchangeName"),
                settings.getString("amqp.username"),
                settings.getString("amqp.password"),
                settings.getString("amqp.routingKey"),
                settings.getString("amqp.componentName")
        );
    }

    @Override
    public String getName() {
        return ACTOR_NAME;
    }
}
