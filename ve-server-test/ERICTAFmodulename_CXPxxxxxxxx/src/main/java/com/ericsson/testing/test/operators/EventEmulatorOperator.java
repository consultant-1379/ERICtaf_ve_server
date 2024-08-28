package com.ericsson.testing.test.operators;

import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.ericsson.duraci.eiffelmessage.sending.exceptions.EiffelMessageSenderException;

public interface EventEmulatorOperator {

    void close();
    
    void sendMessage(EiffelEvent event) throws EiffelMessageSenderException;
    
    void sendMsgPerSecond();
    
    void getEiffelConfiguration();

}
