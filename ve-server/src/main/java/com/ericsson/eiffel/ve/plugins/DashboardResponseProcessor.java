package com.ericsson.eiffel.ve.plugins;

import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;

public interface DashboardResponseProcessor {
    String getMessageType();

    Object process(EiffelMessage message);
}
