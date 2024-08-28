package com.ericsson.eiffel.ve.plugins.debug;



public class DebugSourceActorTest {
	/*
	final private String job = "cip-3727";
	final private String eiffelVersion = "3.0.3.0.2";
	private EiffelMessageService ems;
    private ObjectMapper objectMapper;
    private static final EiffelLog logger = new JavaLoggerEiffelLog(DebugSourceActorTest.class);

    @Before
    public void setUp() throws Exception {
		VETestSupport.setupLogging();
        ems = new EiffelMessageService(eiffelVersion);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldLoadDebugMessages() throws Exception {
        List<EiffelMessageWrapper> wrappers = DebugSourceActor.loadDebugMessages(job, objectMapper);

        assertTrue(wrappers.size() > 0);
    }

    @Test
    public void shouldIterateRepeatedly() throws Exception {
        List<EiffelMessageWrapper> wrappers = DebugSourceActor.loadDebugMessages(job, objectMapper);
        int messageCount = wrappers.size();

        Iterator<String> iterator = DebugSourceActor.getMessageIterator(wrappers, objectMapper, ems);
        EiffelMessage first = parseMessage(iterator.next());
        //The first and last element is the same event and since we no longer generate new EventIds for the elements. 
        //We need to pick another element to test with, hence the -2 instead of -1. 
        Iterators.advance(iterator, messageCount - 2);
        EiffelMessage nextFirst = parseMessage(iterator.next());

        assertFalse(ems.getId(first).equals(ems.getId(nextFirst)));
        assertTrue(first.getEventType().equals("EiffelJobStepStartedEvent"));
        assertFalse(first.getEventType().equals(nextFirst.getEventType()));
    }

    private EiffelMessage parseMessage(String json) {
    	JsonParser jp = new JsonParser();
    	JsonArray array = jp.parse(json).getAsJsonArray();

    	try {
			return new Deserializer(logger).deserialize(array.get(0).getAsJsonObject().get("value").getAsString());
		} catch (MessageDeserializationException e) {
			logger.error("Unable to deserialize Json string: " + json + " due to: " + e.getMessage());
		}
    	return null;
    }
    */
}
