package com.ericsson.eiffel.ve.application;

import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.eiffel.ve.application.dto.EiffelMessageWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EiffelMessageService {

    private final String preferredVersion;

    public EiffelMessageService(String preferredVersion) {
        this.preferredVersion = preferredVersion;
    }

    public EiffelMessageWrapper wrap(EiffelMessage message) {
        return wrap(message, preferredVersion);
    }

    public EiffelMessageWrapper wrap(EiffelMessage message, String version) {
        HashMap<String, EiffelMessage> eiffelMessageVersions = new HashMap<>();
        eiffelMessageVersions.put(version, message);
        EiffelMessageWrapper wrapper = new EiffelMessageWrapper();
        wrapper.setEiffelMessageVersions(eiffelMessageVersions);
        return wrapper;
    }

    public EiffelMessage unwrap(EiffelMessageWrapper wrapper) {
        return unwrap(wrapper, preferredVersion);
    }

    public EiffelMessage unwrap(EiffelMessageWrapper wrapper, String version) {
        Map<String, EiffelMessage> eiffelMessageVersions = wrapper.getEiffelMessageVersions();
        EiffelMessage message = eiffelMessageVersions.get(version);
        if (message != null) {
            return message;
        }
        return eiffelMessageVersions.values().iterator().next();
    }

    public String getId(EiffelMessage message) {
        return message.getEventId().toString();
    }

//    TODO We shouldn't change info in the EiffelMessages?
//    public void setId(EiffelMessage message, String id) {
//        if (id != null) {
//          message.getEventData().put("eventId", id);
//        }
//    }

    public String getParentId(EiffelMessage message) {
//      TODO Is this method supposed to return one or all inputEventIds?
    	
    	List<EventId> inputEventIds = message.getInputEventIds();
    	return inputEventIds.size() > 0 ? inputEventIds.get(0).toString() : "";
    }

//    TODO We shouldn't change info in the EiffelMessages?
//    public void setParentId(EiffelMessage message, String inputEventIds) {
//        if (inputEventIds != null) {
//          message.getEventData().put("parentId", parentId);
//        }
//    }

}
