# Eiffel Visualization Engine (VE) Server

## Configuration
All configuration for the Visualization Engine (VE) is set in a YAML file which must be provided to the server at startup.

The distributed zip file includes a control script `service.sh`, which may be used to start, restart, stop and check the status of the server. The path to a valid configuration file must either be given as an argument, or if this is not the case a file named `settings.yml` must be present in the currrent working directory, and will then be used as the ocnfiguration file for the server.

### Settings File

The configuration file is written in YAML. See below for an example:

    # Default configurations
    common: &default_settings
      akka:
        name: actor-system
      netty:
        hostname: localhost
        port: 8585
        restport: 8586
        webdir: ./web
      amqp:
        host: localhost
        port: 5672
        exchangeName: eiffel.poc
        username: guest
        password: guest
        routingKey: "#"
        componentName: "VE-Server"
      eventRepository:
        uri: http://localhost:8090/eventrepository/restapi/events
      plugins:
        jars: /home/users/abc/simplejar-0.0.3-SNAPSHOT.jar,/home/users/abc/rules/rules-3.5.24.jar


    # Development
    development:
      <<: *default_settings
      netty:
        hostname: localhost
        port: 8585
        restport: 8586
        webdir: src/test/resources/webtest
	  trace:
        level: FINE

    # Production
    production:
      <<: *default_settings

In this example, the configurations for the Production and Development environments both include the `default_settings` configurations. For Development, one of `netty`s settings is changed compared to `default_settings`, and a setting for `trace` is added. (When changing a setting for one if the items, include any other settings once again.) 

Which environment will be used is determined by the system property `eiffel.veserver.env`. Valid values are "production", "test" and "development".


Please remember that the data structure hierachy YAML depends on the indentation.

#### Keys in Settings File

There are a number of items in this settings file.

##### akka
Settings for the concurrency and distributed applications toolkit Akka. Set this to
 
`name: actor-system`,

or if needed use for instance a name which identifies an organization.

##### netty
Web server settings.

      hostname: [web server host]
      port: [web server port]
      restport: [port REST interface] 
      webdir: ./web [location VE client code on web server]

##### ampq
These are settings for the Eiffel [message bus](https://eiffel.lmera.ericsson.se/com.ericsson.duraci/messagebus/introduction/index.html):

    amqp:
      host: [RabbitMQ host]
      port: [port on RabbitMQ host]
      exchangeName: [exchange name]
      username: [username]
      password: [password]
      routingKey: [the bindingKey]
      componentName: [String to identify the sending component outwards].

The `exchangeName` is used to create bindings to message bus queue.

For information about the `routingKey` item, view the documentation about domains and message routing described in Eiffels documentation for [messaging](https://eiffel.lmera.ericsson.se/com.ericsson.duraci/messaging/concepts/domains-and-message-routing.html)

The `componentName` affects the naming of queues used when listening to the Message Bus. Several different parts go into the naming of queues, and this field is used to distinguish this particular VE instance from other components (VE or otherwise) using the same Domain Id.



##### eventRepository
The Eiffel Event Repository

`uri: [URI to Event Repository host]` 

**NOTE:** Do not forget to include port!

##### trace
This is not a mandatory setting. The levels are described [here](http://docs.oracle.com/javase/7/docs/api/java/util/logging/Level.html)

##### plugin 
This is not a mandatory setting.
`plugin.jars` contains a comma separated list of jars with full path. Jars will be loaded by system on startup.