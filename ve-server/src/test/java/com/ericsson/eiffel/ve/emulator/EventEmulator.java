package com.ericsson.eiffel.ve.emulator;

import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.mmparser.clitool.EiffelConfig;
import com.ericsson.duraci.eiffelmessage.sending.MessageSender;
import com.ericsson.duraci.eiffelmessage.sending.exceptions.EiffelMessageSenderException;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class EventEmulator {

    private final Logger logger;
    private final Random random;
    private final ScheduledExecutorService scheduler;
    private final EiffelConfig config;
    private final MessageSender sender;

    public EventEmulator(Logger logger) {
        this.logger = logger;
        random = new Random();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        config = getEiffelConfig();
        sender = new MessageSender.Factory(config).create();
    }

    private EiffelConfig getEiffelConfig() {
        String domainId = System.getProperty("eiffel.domainId", "TAF_Performance_events");
        String exchangeName = System.getProperty("eiffel.exchangeName", "TAF_Performance_events");
        String hostName = System.getProperty("eiffel.hostName");
        return new EiffelConfig(domainId, exchangeName, hostName);
    }

    public static void main(String[] args) throws Exception {
        Logger logger = LoggerFactory.getLogger(EventEmulator.class);
        EventEmulator emulator = new EventEmulator(logger);
        String jobExecutionId = UUID.randomUUID().toString();
        System.out.println("Will generate job with execution UUID: " + jobExecutionId);
        System.out.println("Press <Enter> to start");
        System.in.read();
        emulator.start(jobExecutionId);
    }

    public void start(String jobExecutionId) {
        logger.info("Starting event emulator");
        final EiffelEventsGenerator generator = new EiffelEventsGenerator(config.getDomainId());
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (generator.hasNext()) {
                    sendMessage(generator.next());
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        generator.jobQueued("DemoTest", jobExecutionId)
                .jobStarted(1306)
                .jobStepStarted(100, 100);

        int suiteCount = random.nextInt(3) + 2;
        for (int suiteId = 0; suiteId < suiteCount; suiteId++) {
            generator.testSuiteStarted("", "DemoSuite" + suiteId);
            int testCount = random.nextInt(3) + 2;
            ArrayList<ResultCode> resultCodes = new ArrayList<>();
            for (int testId = 0; testId < testCount; testId++) {
                ResultCode resultCode = randomResult();
                String id = "CIP-" + (random.nextInt(500) + 500) + "_Demo" + testId;
                String name = "DemoTest" + suiteId + "_" + testId;
                generator.testCaseStarted(id, name)
                        .testCaseFinished(resultCode);
            }
            ResultCode resultCode = allSuccess(resultCodes);
            generator.testSuiteFinished(resultCode);
        }

        generator.jobStepFinished(ResultCode.FAILURE);
        generator.jobFinished(ResultCode.FAILURE);

        scheduler.shutdownNow();
    }

    private ResultCode randomResult() {
        return random.nextBoolean() ? ResultCode.SUCCESS : ResultCode.FAILURE;
    }

    private ResultCode allSuccess(Iterable<ResultCode> resultCodes) {
        boolean all = Iterables.all(resultCodes, new Predicate<ResultCode>() {
            @Override
            public boolean apply(ResultCode resultCode) {
                return ResultCode.SUCCESS.equals(resultCode);
            }
        });
        return all ? ResultCode.SUCCESS : ResultCode.FAILURE;
    }

    private void sendMessage(EiffelMessage message) {
        try {
            String className = message.getEvent().getClass().getSimpleName();
            logger.info("Sending: {} ({})", className, message.getEventId());
            sender.send(message);
        } catch (EiffelMessageSenderException e) {
            throw Throwables.propagate(e);
        }
    }

}
