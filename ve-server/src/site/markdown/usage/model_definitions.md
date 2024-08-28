# VE Model Definitions

# Introduction
Models define data structures that can be visualized in client views. Models are compiled by the VE server and sent to the client for display.

# Common model data
Data that are common for all models.

## Model meta data

Some data to describe a model.
	
	modelMetaData = {"Type" : String, "Version" : String}

Example

	modelMetaData = {"Type" : "dice", "Version" : "1.2.3"}

# Statistics-based Models

For those views that use statistics and therefore need little information.

## Ratio distribution model

The ratio distribution model definition.

### Model definition

Note that total count and percentages are not included in the model. It will be calculated by the client if necessary.

	ratioDistributionModel = {"modelMetaData" : modelMetaData, "items" : items}
	modelMetaData = {"Type" : "ratioDistribution", "Version" : "1.0"}
	items = [item1, item2, ..]
	item = {"label" : String, "value" : Integer}

Example

	{
		"modelMetaData" : {"Type" : "ratioDistribution", "Version" : "1.0"}, 
		"items" : [
		{"label" : "eventType1", "value" : 10},
		{"label" : "eventType2", "value" : 20}
		]
	}


### Users
Can be used by the following visualization types

* Pie chart
* Bar chart (not implemented)



# Node-based Models
For those view that use the d3 nodes and therefore need more information.

## Directed acyclic graph model

  
### Model definition

	directedAcyclicGraphModel = {"modelMetaData" : modelMetaData, "items" : items}
	modelMetaData = {"Type" : "directedAcyclicGraphModel", "Version" : "1.0"}
	items = [graph1, graph2, ..]
	graph1 = [item1, item3];
	item1 = {
          "id"         : 0// the unique id of the data
          "type"       : "events", // the type of the flow of event, or job
          "uniqueId"   : "0c7d5153-7980-44e9-8542-b6292f22a1c8" // the event ID or JobExecutionId depends on if the "type" of data is events or jobs.
          "title"      : “EiffelBaselineDefinedEvent” // Default is event.eventType.  Title for the nodes.
          "information": {Key1: Value1, Key2: Value2}, // the text will be displayed
          "status"     : "SUCCESS", // the STATUS or UNKNOWN, derived from event.eventData.resultCode
          "connection" : [      //children list and their link location
                         {id:2, type:inputEventId},//The ID “to” nodes
                         {id:3, type:inputEventId}
                       ],
          "associatedEvents" : [eventId, eventId, ....] // List of events that are associated with this node. In the case the node represents many events.
       };
 
### Users

Can be used by the following visualization types

* Flow Chart
* Cluster chart

Example

Higher view of model example
 
	{
	"modelMetaData" : {"Type" : "directedAcyclicGraphModel", "Version" : "1.0.1"},
	"items" : [graph1, graph2, ..]
	}
 
In side the "items" tag will be an array of models. For example: graph1 will be displayed in the FlowChart as one set of flows, graph 2 will be another set which will be displayed in the VE on a seperate tab.

graph1 for example will be a json array of event objects. These object will be sorted by EventTime and will be an aggregatation of each events : EiffelJobStartedEvent, EiffelJobFinishedEvent, EiffelJobModifiedEvent etc. Each event is connected together by inputEventId which will match the eventId of the eiffel event.


#### Property Definition

id  - Will be set by the server from 0 to  ...

type - For flow this will be "events"

uniqueId - Will be the eventId of the EiffelJobStartedEvent

title -  Will be the eventtype of the eiffelEvent

information  - This will be an object with domainId and status taken from the eiffelEvent 
(Note status can only be got from the EiffelJobFinishedEvent) --(possible future this may be changeable)

status -  The status from the EiffelJobFinishedEvent

connection - This is an array of Objects with id as the server defined event and type is the inputEventId to the next node this item is connected.
This is used to connect a line to the next node that it is connected. This could be muliple nodes. Empty array means it has no connecting nodes
 
The items in each graph data (graph1, graph2) should be sorted based on "eventTime".


Example shows the model with items tag expanded

	{
	  "modelMetaData":{
	  "Type":"directedAcyclicGraphModel",
      "Version":"1.2.3"
	},
	"items":[
      [
         {
            "id":0,
            "type":"events",
            "uniqueId":"0c7d5153-7980-44e9-8542-b6292f22a1c8",
            "title":"EiffelBaselineDefinedEvent",
            "information":{
               "domainId":"kista",
               "eventData.resultCode":"Unknown"
            },
            "status":"UNKNOWN",
            "connection":[
               {
                  "id":2,
                  "type":"inputEventId"
               },
               {
                  "id":3,
                  "type":"inputEventId"
               }
            ]
         },
         {
            "id":2,
            "type":"events",
            "uniqueId":"0c7d5153-7980-44e9-8542-b6292f22a1c0",
            "title":"EiffelBaselineDefinedEvent",
            "information":{
               "domainId":"kista",
               "eventData.resultCode":"Success"
            },
            "status":"SUCCESS",
            "connection":[]
         },
         {
            "id":3,
            "type":"events",
            "uniqueId":"0c7d5153-7980-44e9-8542-b6292f22a1d1",
            "title":"EiffelBaselineDefinedEvent",
            "information":{
               "domainId":"kista",
               "eventData.resultCode":"Success"
            },
            "status":"SUCCESS",
            "connection":[]
         }
      ],
      [
         {
            "id":1,
            "type":"events",
            "uniqueId":"0c7d5153-7980-44e9-8542-b6292f22a1c9",
            "title":"EiffelBaselineDefinedEvent",
            "information":{
               "domainId":"kista",
               "eventData.resultCode":"Success"
            },
            "status":"SUCCESS",
            "connection":[]
         }
      ]
	]
	}


### Request parameters

When sending a request the following parameters and query options should be included.

<table>
  <tbody>
    <tr>
      <th>Parameters</th>
      <th>Example</th>
      <th>Comment</th>
    </tr>
    <tr>
      <td>
        <p>model=modelName</p>
      </td>
      <td>
        <p>model=<span>directedAcyclicGraph</span>
        </p>
      </td>
      <td> </td>
    </tr>
    <tr>
      <td>
        <p>modelVersion=String</p>
      </td>
      <td>
        <p>modelVersion="1.2.3"</p>
      </td>
      <td> </td>
    </tr>
    <tr>
      <td>
        <p>query=queryString</p>
      </td>
      <td>
        <p>query=<span>eventType</span>
        </p>
      </td>
      <td>
        <p>The query language is defined here <ac:link>
            <ri:page ri:content-title="VE Query language"/>
          </ac:link>.</p>
      </td>
    </tr>
    <tr>
      <td colspan="1">updateInterval=Integer</td>
      <td colspan="1">updateInterval=10</td>
      <td colspan="1">Update interval in seconds. Only for live data.</td>
    </tr>
  </tbody>
</table>


Query options.

<table>
  <tbody>
    <tr>
      <th>Query Options</th>
      <th>Example</th>
      <th>Comment</th>
    </tr>
    <tr>
      <td colspan="1">base=Key</td>
      <td colspan="1">base=eventId</td>
      <td colspan="1">Eiffel event field used for grouping / clustering</td>
    </tr>
    <tr>
      <td colspan="1">title=Key</td>
      <td colspan="1">
        <p>title=<span style="background-color: transparent;line-height: 1.4285715;">eventType</span>
        </p>
      </td>
      <td colspan="1"> </td>
    </tr>
    <tr>
      <td colspan="1">information=Fields</td>
      <td colspan="1">
        <p>information=<span style="background-color: transparent;line-height: 1.4285715;">domainId,</span>
          <span style="background-color: transparent;line-height: 1.4285715;">eventData.resultCode</span>
        </p>
      </td>
      <td colspan="1">Specifies what fields to get information from</td>
    </tr>
    <tr>
      <td colspan="1">includeConnections=Boolean</td>
      <td colspan="1">includeConnections=true</td>
      <td colspan="1">Whether or not to include the connection parameter in the model</td>
    </tr>
    <tr>
      <td colspan="1">dagAggregation=Boolean</td>
      <td colspan="1">dagAggregation=false</td>
      <td colspan="1">Aggregation of DAGs (graphs). For a Flow Chart this is false. For a Cluster Chart it is true, meaning there will only be one DAG (graph) in the model data structure.</td>
    </tr>
    <tr>
      <td colspan="1">
        <span>maxNumberOfDags=Integer</span>
      </td>
      <td colspan="1">
        <span>maxNumberOfDags=10</span>
      </td>
      <td colspan="1">
        <span>Depending on the query the result might be everything between 0 - Integer graphs/DAGs. NOTE: if dagAggregation is true maxNumberOfDags will be 1.</span>
      </td>
    </tr>
    <tr>
      <td colspan="1">
        <p>pageNo=Integer</p>
      </td>
      <td colspan="1">
        <p>pageNo=1</p>
      </td>
      <td colspan="1"> </td>
    </tr>
    <tr>
      <td colspan="1">
        <p>pageSize=Integer</p>
      </td>
      <td colspan="1">
        <p>pageSize=20</p>
      </td>
      <td colspan="1">
        <p>In this context pageSize would mean the number of nodes to be displayed in one DAG.</p>
      </td>
    </tr>
    <tr>
      <td colspan="1">
        <p>sortField=<span>eventTime </span>
        </p>
      </td>
      <td colspan="1" style="text-align: center;">-</td>
      <td colspan="1">
        <p>Currently only eventTime is supported by ER.</p>
      </td>
    </tr>
    <tr>
      <td colspan="1">
        <p>sortOrder=<span>descending </span>
        </p>
      </td>
      <td colspan="1" style="text-align: center;">-</td>
      <td colspan="1">
        <p>Currently only descending is supported by ER.</p>
      </td>
    </tr>
    <tr>
      <td colspan="1">startDate=string (YYYYMMDD)</td>
      <td colspan="1">startDate="20140302"</td>
      <td colspan="1">Start of the date interval that the user is interested in.</td>
    </tr>
    <tr>
      <td colspan="1">endDate=string (YYYYMMDD)</td>
      <td colspan="1">endDate="20140302"</td>
      <td colspan="1">End of the date interval that the user is interested in.</td>
    </tr>
  </tbody>
</table>

A complete request for historical data would look like

	http://host:port/historicaldata/queryhandler/?model=directedAcyclicGraph&modelVersion=1.0&query=eventData.changeSet.team=superteam&base=eventId&title=eventType&information=domainId,eventData.resultCode&includeConnections=true&dagAggregation=false&maxNumberOfDags=2&pageNo=1&pageSize=100&sortField=eventTime&sortOrder=descending&startDate=20140302&endDate=20140302

A live data subscription request would look like

	{
	"method" : "PUT",
	"eventURI" : "ve:livedata/subscriptions/e098b740-9348-11e3-baa8-0800200c9a66",
	"version" : "1.0",
	"eventBody" : {
			"model" : "directedAcyclicGraph",
			"modelVersion" : "1.0",
			"query" : "eventData.changeSet.team=superteam",
			"queryOptions" : {
				"base" : "eventId",
				"title" : "eventType",
				"information" : ["domainId", "eventData.resultCode"],
				"includeConnections" : true,
				"dagAggregation" : false,
				"maxNumberOfDags" : 2,
				"pageNo" : 1, 
				"pageSize" : 100, 
				"sortField" : "eventTime", 
				"sortOrder" : "descending",
				"startDate" : "20140302",
				"endDate" : "20140302"
				},
			"updateInterval" : 10
    		}
	}


### Typical sequence diagram

![Typical sequence diagram](../images/RetrieveDataFromER.jpg "Retrieve data from ER.")


# Other Models

## VE Eiffel Event model

The eiffel event model definition. Eiffel events are inserted verbatim. If the Eiffel message contains several version only one of the versions should be inserted into the model. The version to insert is indicated by the client.


### Model definition

	eiffelEventModel = {"modelMetaData" : modelMetaData, "items" : items}
	modelMetaData = {"Type" : "eiffelEvent", "Version" : "1.0"}
	items = [item1, item2, ..]
	item = {"eiffelEvent" : eiffelEvent}

Example

	{
		"modelMetaData" : {"Type" : "eiffelEvent", "Version" : "1.0"}, 
		"items" : [
			{"eiffelEvent" : e1},
			{"eiffelEvent" : e2}
		]
	}

### Users

Can be used by any view that want to display all parameters of an Eiffel event.

## Matrix model

The matrix model definition

### Model definition


	matrixModel = {"modelMetaData" : modelMetaData, "columns" : columns, "items" : items}
	modelMetaData = {"Type" : "matrix", "Version" : "1.0"}
	columns = [Key, Key,...]
	items = [row, row, ..]
	row = [value, value, ..]

Example

	{
		"modelMetaData" : {"Type" : "matrix", "Version" : "1.0"},
		"columns" : [eventType, eventId, eventData.resultCode]
		"items" : [
			["EiffelJobFinishedEvent", "ad27c2c4-ec41-488e-93f2-fc2702b32f21", "SUCCESS"],
			["EiffelSCMChangedEvent", "ad27c2c4-ec41-488e-93f2-fc2702b32f22", "-"]
		]
	}

### Users

Can be used by the following visualization types

* Table chart