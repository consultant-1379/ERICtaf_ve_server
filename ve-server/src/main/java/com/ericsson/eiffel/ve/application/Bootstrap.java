package com.ericsson.eiffel.ve.application;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.reflections.Reflections;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.VEModel;
import com.ericsson.eiffel.ve.api.internal.EventRepositoryAccessor;
import com.ericsson.eiffel.ve.application.handler.LiveDataSubscriptionHandler;
import com.ericsson.eiffel.ve.application.routing.EiffelMessageRouter;
import com.ericsson.eiffel.ve.application.routing.MessageRouter;
import com.ericsson.eiffel.ve.infrastructure.config.Settings;
import com.ericsson.eiffel.ve.plugins.DashboardActorFactory;
import com.ericsson.eiffel.ve.plugins.DashboardResponseProcessor;
import com.ericsson.eiffel.ve.plugins.VEMessageHandler;
import com.ericsson.eiffel.ve.plugins.er.EventRepository;
import com.ericsson.eiffel.ve.web.VEService;
import com.ericsson.eiffel.ve.web.WebServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;

/**
 *
 */
public class Bootstrap {

    private static Bootstrap INSTANCE = new Bootstrap();

    private static final EiffelLog logger = new JavaLoggerEiffelLog(Bootstrap.class);

    private WebServer server;
    private ActorSystem actorSystem;
    private Map<String, ActorRef> pluggedActors;
    private ObjectMapper objectMapper;
    private MessageRouter messageRouter;
    private VEService veService;
    private SetMultimap<String, VEMessageHandler> messageHandlers;
    private Map<String, Class<? extends VEModel>> models;
    private Map<String, VEModel> modelInstancesForREST;
    private DashboardResponseProcessor responseProcessor;
    private EiffelMessageService eiffelMessageService;
    private EventRepositoryAccessor eventRepositoryAccessor = null;
    private Logger topLogger = null; // Keep a reference to prevent GC
    private Settings settings;

    private Bootstrap() {
    }

    public WebServer getServer() {
        return server;
    }

    public MessageRouter getMessageRouter() {
        return messageRouter;
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public ActorRef getPluggedActor(String name) {
        return pluggedActors.get(name);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public VEService getVEService() {
        return veService;
    }
    
    public EventRepositoryAccessor getEventRepositoryAccessor() {
    	return eventRepositoryAccessor;
    }
    
    public Class<? extends VEModel> getModel(String modelName) {
    	return models.get(modelName);
    }
    
    public VEModel getRESTInstanceForModel(String modelName) {
    	return modelInstancesForREST.get(modelName);
    }

    public EiffelMessageService getEiffelMessageService() {
        return eiffelMessageService;
    }

    public Set<VEMessageHandler> getMessageHandlers(String resource) {
    	Set<VEMessageHandler> handlers = new HashSet<VEMessageHandler>();
    	for(String key : messageHandlers.keySet())
    		if(resource.startsWith(key))
    			handlers.addAll(messageHandlers.get(key));
    	
        return handlers;
    }

    public DashboardResponseProcessor getResponseProcessor() {
        return responseProcessor;
    }

    public static Bootstrap getInstance() {
        return INSTANCE;
    }
    
    public Settings getSettings() {
    	return settings;
    }

    public void init(DashboardConfiguration configuration, Settings settings) {
        this.settings = settings; 
        setupLogging(settings);
        startAkka(settings);
        startServices(settings);
        setupMessageHandlers(settings, configuration);
        setupModels(settings);
        setupActors(settings, configuration);
        setupEventProcessor(settings, configuration);
        startWebServer(settings);
    }

    private void setupLogging(Settings settings){
    	String levelStr = settings.getString("trace.level");
    	Level level = null;
    	try {
    		level = Level.parse(levelStr);
    	} catch (NullPointerException e) {
    		logger.info("Could not use tracelevel specified in configuration. Using default settings.");
    		return;
    	} catch (IllegalArgumentException  e){
    		logger.error("Could not parse trace.level '" + levelStr + "' from settings. Using default settings.");
    		return;
    	}
    	
        //get the top Logger:
        topLogger = Logger.getLogger("");
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

    private void startAkka(Settings settings) {
        actorSystem = ActorSystem.create(settings.getString("akka.name"));
    }

    private void startWebServer(Settings settings) {
        server = new WebServer(settings);
        server.start();
    }

    private void startServices(Settings settings) {
        this.objectMapper = new ObjectMapper();
        this.eiffelMessageService = new EiffelMessageService(settings.getString("eiffel.version"));
        this.messageRouter = new EiffelMessageRouter(objectMapper, eiffelMessageService);
        this.veService = new VEService(actorSystem, this.messageRouter);
        if(settings.getString("eventRepository.uri") != null)
        	this.eventRepositoryAccessor = new EventRepository(settings.getString("eventRepository.uri"));
    }

    private void setupMessageHandlers(Settings settings, DashboardConfiguration configuration) {
        Set<VEMessageHandler> handlerSet = configuration.getMessageHandlers();
//        if (settings.getBoolean("debug.enabled")) {
//            handlerSet.add(new DebugSourceHandler());
//        }
//        handlerSet.add(new SubscribeHandler());
//        handlerSet.add(new UnsubscribeHandler());
//        handlerSet.add(new HistoricalDataHandler());
        handlerSet.add(new LiveDataSubscriptionHandler());
        messageHandlers = HashMultimap.create();
        for (VEMessageHandler messageHandler : handlerSet) {
            logger.info("Registering message handler: " +
                    messageHandler.getResourceName() +
                    "[" + messageHandler.getClass().getName() + "]");
            messageHandlers.put(messageHandler.getResourceName(), messageHandler);
        }
    }
    
    private void setupModels(Settings settings) {
    	models = new HashMap<String, Class<? extends VEModel>>();
    	modelInstancesForREST = new HashMap<String, VEModel>();
    	Reflections reflections = new Reflections("com.ericsson");    
    	Set<Class<? extends VEModel>> classes = reflections.getSubTypesOf(VEModel.class);
    	for(Class<? extends VEModel> modelClass : classes) {
    		try {
    			if(modelClass.getCanonicalName() != null) {
        			logger.info("Found model class: " + modelClass.getCanonicalName());
    				final Constructor<?> dsConst = modelClass.getDeclaredConstructor();
    				VEModel modelInstance = (VEModel) dsConst.newInstance();
    				String modelName = modelInstance.getModelName();
    				logger.debug("Model name: "+modelName);
    				models.put(modelName, modelClass);
    				modelInstance.setEventRepositoryAccessor(eventRepositoryAccessor);
    				modelInstancesForREST.put(modelName, modelInstance);
    			}
			} catch (NoSuchMethodException e) {
				logger.error(e.getMessage());
			} catch (SecurityException e) {
				logger.error(e.getMessage());
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage());
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage());
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage());
			} catch (InstantiationException e) {
				logger.error(e.getMessage());
			}
    	}
    }

    private void setupActors(Settings settings, DashboardConfiguration configuration) {
        Set<DashboardActorFactory> factories = configuration.getActorFactories();
        ImmutableMap.Builder<String, ActorRef> mapBuilder = ImmutableMap.builder();
        for (DashboardActorFactory factory : factories) {
            Props props = factory.makeProps(settings);
            String name = factory.getName();
            logger.info("Registering actor: " + name +
                    "[" + props.actorClass().getName() + "]");
            ActorRef actorRef = actorSystem.actorOf(props, name);
            mapBuilder.put(name, actorRef);
        }
        pluggedActors = mapBuilder.build();
    }

    private void setupEventProcessor(Settings settings, DashboardConfiguration configuration) {
        responseProcessor = configuration.getResponseProcessor();
    }

    public void shutdown() {
        server.stop();
        actorSystem.shutdown();
        actorSystem.awaitTermination();
    }

}
