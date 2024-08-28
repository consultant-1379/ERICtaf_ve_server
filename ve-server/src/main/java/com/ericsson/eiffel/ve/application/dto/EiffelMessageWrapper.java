package com.ericsson.eiffel.ve.application.dto;

import java.util.Map;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;

public class EiffelMessageWrapper {

    private Map<String, EiffelMessage> eiffelMessageVersions;

    public Map<String, EiffelMessage> getEiffelMessageVersions() {
        return eiffelMessageVersions;
    }

    public void setEiffelMessageVersions(Map<String, EiffelMessage> eiffelMessageVersions) {
        this.eiffelMessageVersions = eiffelMessageVersions;
    }
}
