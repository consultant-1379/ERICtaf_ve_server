package com.ericsson.eiffel.ve.emulator;

import com.ericsson.duraci.datawrappers.Environment;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.messages.events.*;
import com.google.common.base.Throwables;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.SynchronousQueue;

public final class EiffelEventsGenerator implements Iterator<EiffelMessage> {

    private final SynchronousQueue<EiffelMessage> messages = new SynchronousQueue<>();
    private final String domainId;

    private String jobInstance;
    private int jobExecutionNumber;
    private ExecutionId jobExecutionId;
    private ExecutionId jobStepExecutionId;
    private ExecutionId testSuiteExecutionId;
    private ExecutionId testCaseExecutionId;

    private EventId jobQueuedEventId;
    private EventId jobStartedEventId;
    private EventId jobStepStartedEventId;
    private EventId testSuiteStartedEventId;
    private EventId testCaseStartedEventId;

    public EiffelEventsGenerator(String domainId) {
        this.domainId = domainId;
    }

    private EiffelMessage toMessage(EiffelEvent event, EventId parentEventId) {
        return EiffelMessage.Factory.configure(domainId, event)
                .addInputEventIds(parentEventId)
                .create();
    }

    private void put(EiffelMessage message) {
        try {
            messages.put(message);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public EiffelMessage next() {
        try {
            return messages.take();
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public EiffelEventsGenerator jobQueued(String jobInstance, String jobExecutionId) {
        EiffelJobQueuedEvent event = EiffelJobQueuedEvent.Factory.create(jobInstance, jobExecutionId);
        this.jobInstance = jobInstance;
        this.jobExecutionId = event.getJobExecutionId();

        EiffelMessage message = toMessage(event, null);
        jobQueuedEventId = message.getEventId();
        put(message);
        return this;
    }

    public EiffelEventsGenerator jobStarted(int jobExecutionNumber) {
        EiffelJobStartedEvent event = EiffelJobStartedEvent.Factory.create(jobInstance, jobExecutionId, jobExecutionNumber);
        this.jobExecutionId = event.getJobExecutionId();
        this.jobExecutionNumber = jobExecutionNumber;
        EiffelMessage message = toMessage(event, jobQueuedEventId);
        jobStartedEventId = message.getEventId();
        put(message);
        return this;
    }

    public EiffelEventsGenerator jobStepStarted(Integer expectedDurationInSeconds, Integer expectedNumberOfChildEvents) {
        EiffelJobStepStartedEvent event = EiffelJobStepStartedEvent.Factory.create(jobExecutionId, expectedDurationInSeconds, expectedNumberOfChildEvents);
        jobStepExecutionId = event.getJobStepExecutionId();

        EiffelMessage message = toMessage(event, jobStartedEventId);
        jobStepStartedEventId = message.getEventId();
        put(message);
        return this;
    }

    public EiffelEventsGenerator testSuiteStarted(String type, String name) {
        EiffelTestSuiteStartedEvent event = EiffelTestSuiteStartedEvent.Factory.create(jobStepExecutionId, type, name);
        testSuiteExecutionId = event.getTestSuiteExecutionId();

        EiffelMessage message = toMessage(event, jobStepStartedEventId);
        testSuiteStartedEventId = message.getEventId();
        put(message);
        return this;
    }

    public EiffelEventsGenerator testCaseStarted(String testId, String name) {
        EiffelTestCaseStartedEvent event = EiffelTestCaseStartedEvent.Factory.create(testSuiteExecutionId, testId, name, Collections.<Environment>emptyList());
        testCaseExecutionId = event.getTestCaseExecutionId();

        EiffelMessage message = toMessage(event, testSuiteStartedEventId);
        testCaseStartedEventId = message.getEventId();
        put(message);
        return this;
    }

    public EiffelEventsGenerator testCaseFinished(ResultCode resultCode) {
        EiffelTestCaseFinishedEvent event = EiffelTestCaseFinishedEvent.Factory.create(resultCode, null, testCaseExecutionId);
        put(toMessage(event, testCaseStartedEventId));
        return this;
    }

    public EiffelEventsGenerator testSuiteFinished(ResultCode resultCode) {
        EiffelTestSuiteFinishedEvent event = EiffelTestSuiteFinishedEvent.Factory.create(resultCode, null, testSuiteExecutionId);
        put(toMessage(event, testSuiteStartedEventId));
        return this;
    }

    public EiffelEventsGenerator jobStepFinished(ResultCode resultCode) {
        EiffelJobStepFinishedEvent event = EiffelJobStepFinishedEvent.Factory.create(resultCode, null, jobStepExecutionId);
        put(toMessage(event, jobStepStartedEventId));
        return this;
    }

    public EiffelEventsGenerator jobFinished(ResultCode resultCode) {
        EiffelJobFinishedEvent event = EiffelJobFinishedEvent.Factory.create(jobInstance, jobExecutionId, jobExecutionNumber, resultCode, null);
        put(toMessage(event, jobStartedEventId));
        return this;
    }

}
