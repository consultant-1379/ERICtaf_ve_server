package com.ericsson.eiffel.ve.plugins.er;

import com.ericsson.duraci.eiffelmessage.deserialization.Deserializer;
import com.ericsson.duraci.eiffelmessage.deserialization.exceptions.MessageDeserializationException;
import com.ericsson.duraci.eiffelmessage.serialization.Serializer;
import com.ericsson.duraci.eiffelmessage.serialization.printing.exceptions.MessagePrintingException;
import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.ericsson.eiffel.ve.api.internal.EventRepositoryAccessor;
import com.ericsson.eiffel.ve.application.Bootstrap;
import com.ericsson.eiffel.ve.application.EiffelMessageService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

public class EventRepository implements EventRepositoryAccessor {

    private static final EiffelLog logger = new JavaLoggerEiffelLog(EventRepository.class);

    private Client client;
    private WebTarget base;

    public EventRepository(String uri) {
        this(
                uri,
                Bootstrap.getInstance().getEiffelMessageService()
        );
    }

    EventRepository(
            String uri,
            EiffelMessageService eiffelMessageService) {
        client = ClientBuilder.newClient();
        base = client.target(uri).path("events");
    }

    /* (non-Javadoc)
     * @see com.ericsson.eiffel.ve.plugins.er.EventRepositoryAccessor#findEvent(java.lang.String)
	 */
    @Override
    public String findEvent(String id) {
        logger.debug("Querying the ER for event: " + id);
        return requestEvents(base.path(id));
    }

    /* (non-Javadoc)
	 * @see com.ericsson.eiffel.ve.plugins.er.EventRepositoryAccessor#findDownstream(java.lang.String)
	 */
    @Override
    public String findDownstream(String id) {
        logger.debug("Querying the ER for downstream events to event: " + id);
        return requestEvents(base.path(id).path("downstream"));
    }

    /* (non-Javadoc)
	 * @see com.ericsson.eiffel.ve.plugins.er.EventRepositoryAccessor#findUpstream(java.lang.String)
	 */
    @Override
    public String findUpstream(String id) {
        logger.debug("Querying the ER for upstream events to event: " + id);
        return requestEvents(base.path(id).path("upstream"));
    }

    /* (non-Javadoc)
	 * @see com.ericsson.eiffel.ve.plugins.er.EventRepositoryAccessor#findEvents(java.lang.String)
	 */
    @Override
    public String findEvents(String query) {
        logger.debug("Querying the ER for events based filters");
        return requestEvents(setQuery(base, query));
    }

    private WebTarget setQuery(WebTarget target, String query) {
        URI uri = target.getUriBuilder().replaceQuery(query).build();
        return client.target(uri);
    }

    private String requestEvents(WebTarget target) {
        logger.debug("Sending query to ER");
        Invocation.Builder request = target
                .request(MediaType.APPLICATION_JSON_TYPE);
        Response response = request.get();

        return versionNeutral(response.readEntity(String.class));
    }

    /*
     * Changes the items(events) from the response from the ER to version neutral versions. 
     */
    private String versionNeutral(String response) {
        Deserializer deserializer = new Deserializer();
        Serializer serializer = new Serializer();
        JsonArray items = new JsonArray();
        JsonParser jp = new JsonParser();
        JsonObject jresponse = jp.parse(response).getAsJsonObject();

        for (JsonElement item : jresponse.get("items").getAsJsonArray()) {
            try {
                items.add(
                        jp.parse(
                                serializer.pretty(deserializer.deserialize(item.getAsString())).versionNeutralLatestOnly().print())
                );
            } catch (MessagePrintingException | JsonSyntaxException | MessageDeserializationException e) {
                logger.error("Failed to get the version neutral version of the event.");
            }
        }
        jresponse.add("items", items);
        return jresponse.toString();
    }
}
