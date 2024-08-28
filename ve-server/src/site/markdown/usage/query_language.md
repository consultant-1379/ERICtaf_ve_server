# VE Query Language

## Query definition

A query defines one or more key or key-value pair to match eiffel events against.

To match against existence of a specific key in the event, the query specifies a single key.

	eventType

To specify that a key must also have a certain value the key=value form is used in the query.

	eventType=EiffelJobFinishedEvent

If the value belongs to a nested key a dot, '.', is used to separate keys.

	eventData.jobExecutionId=276
	eventData.optionalParameters.proj=main

An AND relation between key=value pairs can be defined by using '&&' in the query.

	eventType=EiffelJobFinishedEvent&&eventData.jobExecutionId=276

OR relations between key=value pairs are denoted by '||' in the query

	eventType=EiffelJobFinishedEvent||eventType=EiffelJobStartedEvent

AND and OR can be mixed in one query. The query must be flat, no nested logical expressions are allowed. AND have precedence over OR.

	eventType=EiffelJobFinishedEvent||eventType=EiffelJobStartedEvent&&eventData.jobExecutionId=276

A key=value pair can be negated by prefixing it with '!'.

	eventType=EiffelJobFinishedEvent&&!eventData.jobExecutionId=276
 
Example queries are based on this sample event

	{
		"eventType":"EiffelJobFinishedEvent",
    	"eventData": {
    	    "jobInstance":"LMBaselineBuilder_rnc_main_89_1_Trigger",
    	    "jobExecutionId":"276",
    	    "resultCode": "SUCCESS",      
    	    "inputEventIds": ["233744bf-5daf-4d32-839d-cc73e6000fa8", "f1759b0c-a107-4b82-9a4d-be7fd04f6ec7"],     "optionalParameters": {
          	  	"org": "rnc",
           	 	"proj": "main"
        	}
    	}
	}

Future improvement suggestions

Add semantics to match against individual items in a list.

Add possibility to give the value as a regular expression. Define a subset of the regular expression syntax that should be allowed in queries.

### Issues

The characters '.' and ':' choosen for separating keys and values can appear in the keys and values themselves which will cause the parsing to fail. In the new implementation characters or character sequences should be selected that are guaranteed to be unique.
 