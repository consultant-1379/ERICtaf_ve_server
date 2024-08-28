# VE Visualization Configuration Formats

## General View Configuration Data Format

### Configuration format

	{
    "id": "120398adb9d781120398adb9d781", //unique widget configuration ID
    "author": "erobsvi",
    "type": "cluster",
    "model": "ratioDistributionModel"
    "title": "WMR Latest build",          //unique
    "subscription": ["key1:value1&&key2:value2", "key2:value1"],
    "queryOptions": [],
    "span": 4,
    "aspectRatio": "default",
    "tags": [
        "WMR",
        "Design"
    ],
     "typeSettings": typeSettings // specific for each view (see sibling pages for more info)
	}



## Cluster Chart type Configuration Data Format

typeSettings

	{
    	base: Key, // On what basis should we cluster.
    	summarize: Boolean, // Summarize into single node
    	fadeoutTime: Integer,  // Time until node fades out
    	nodesPerRow: Integer   // nodes per row
	}


Example

typeSettings

	{
    	base: eventId, // On what basis should we cluster.
    	summarize: true, // Summarize into single node
    	fadeoutTime: 0,  // Time until node fades out
    	nodesPerRow: 2   // nodes per row
	}


## Flow Chart type Configuration Data Format


### Configuration format

typeSettings

	{
	    numOfEvents: 50,   // the number of records extracted from ER
    	maxNumOfNodes: 10, // the number of nodes shown per tab
    		numOfFlows: 2,     // the number of tabs available
    	nodeHeight: 60,
    	nodeWidth: 180
 	}
 
### Outstanding issues

Q: Should numOfRecords exist? It determines how many events to fetch from ER and will not be useful after model move to server (Hongbo, please correct me if I have misunderstood the purpose of this).

Q: Should the Flowchart view be refactorized to not search for issues based on everything in the subscriptions, but only to allow a single event to be selected (e.g. SCM change Event) and draw everything from that?



## Pie Chart type Configuration Data Format

### Configuration format

typeSettings

	{
	    base: Key, //On what basis should we create slices on.
	    showLabel: Boolean // should we show text in pie slices / on hover
	}

Example

typeSettings

	{
	    "base": "eventData.resultCode",
	    "showLabel": true
	}



## Table Chart type Configuration Data Format


### Configuration format
typeSettings

	{
    	"columnsList": columns_list; //
    	"pagination":pagination;      // non-negative integer, 0 means none
    	"sortColumn":sort_column;    // string of name of column to sort on
		"sortType":sort_type         //"ascending" or "descending"
	}

columns_list can be all or a subset of the following:

	['domainId', 'eventId', 'eventTime', eventType', 'eventData.jobInstance', ...] //and whatever other fields may be added to the MessageBus template.

### Outstanding issues

Q. Should pagination 0 be allowed. It currently is not allowed in the settings view. Alternatively a more dynamic approach to pagination can be taken, by allowing aspectRatio to override the given pagination and determine the allowed number of items/page.


## Dashboard Configuration Data Format

### Configuration format



Example

	{
		"id": "8as762aw08u0ca7s8as762aw08u0ca7s",
    	"title": "Robin's dashboard",  //unique
    	"author": "erobsvi",
    	"view": "dashboard",        //is this field necessary?
    	"tags": [
        	"LTE",
        	"Design"
    	],
     	"viewIds": [
        	"120398adb9d781120398adb9d781",
       	 	"081401087aajapife0e3e8f80f3f2b2"
    	]
	 }

Outstanding issues
 
{
		"id": "8as762aw08u0ca7s8as762aw08u0ca7s",
    	"title": "Robin's dashboard",  
    	"author": "erobsvi",
    	"view": "dashboard",        
    	"tags": [
        	"LTE",
        	"Design"
    	],
     	"viewIds": [
        	"120398adb9d781120398adb9d781",
       	 	"081401087aajapife0e3e8f80f3f2b2"
    	]
	 }