# Introduction


# Visualization Configuration Service

This REST service provides a way for the client to store and retrieve Dashboard and View configurations. It also provides a way to retrieve the options for a specific visualization type. It uses HTTP as underlying layer.

### Dashboard resource

<table>
  <tbody>
    <tr>
      <th>Resource</th>
      <th>Method</th>
      <th>Description</th>
      <th colspan="1">Request body</th>
      <th colspan="1">Response code on success</th>
      <th colspan="1">Response body on success</th>
    </tr>
    <tr>
      <td>configuration/dashboards/&lt;id&gt;</td>
      <td>GET</td>
      <td>Retrieve a dashboard configuration from the configuration store.</td>
      <td colspan="1">-</td>
      <td colspan="1">200 OK</td>
      <td colspan="1">
        <span>A dashboard configuration.</span>
      </td>
    </tr>
    <tr>
      <td colspan="1">
        <span>configuration/dashboards/?&lt;options&gt;</span>
      </td>
      <td colspan="1">GET</td>
      <td colspan="1">
        <span>Retrieve a list of dashboards Ids from the configuration store.</span>
      </td>
      <td colspan="1">-</td>
      <td colspan="1">200 OK</td>
      <td colspan="1">List of dashboard Ids and titles.</td>
    </tr>
    <tr>
      <td colspan="1">
        <span>configuration/dashboards/&lt;id&gt;</span>
      </td>
      <td colspan="1">PUT</td>
      <td colspan="1">Full update of a dashboard configuration.</td>
      <td colspan="1">A dashboard configuration (including Id)</td>
      <td colspan="1">200 OK</td>
      <td colspan="1">-</td>
    </tr>
    <tr>
      <td colspan="1">
        <span>configuration/dashboards</span>
      </td>
      <td colspan="1">POST</td>
      <td colspan="1">Store a new dashboard configuration.</td>
      <td colspan="1">A dashboard configuration without an Id.</td>
      <td colspan="1">201 Created</td>
      <td colspan="1">
        <span>A dashboard Id</span>
      </td>
    </tr>
    <tr>
      <td colspan="1">
        <span>configuration/dashboards/&lt;id&gt;</span>
      </td>
      <td colspan="1">DELETE</td>
      <td colspan="1">Delete a dashboard configuration.</td>
      <td colspan="1">-</td>
      <td colspan="1">
        <span style="color: rgb(0,0,0);">204 No Content</span>
      </td>
      <td colspan="1">-</td>
    </tr>
  </tbody>
</table>

### View resource

<table>
  <tbody>
    <tr>
      <th>Resource</th>
      <th>Method</th>
      <th>Description</th>
      <th colspan="1">Request body</th>
      <th colspan="1">Response code on success</th>
      <th colspan="1">Response body on success</th>
    </tr>
    <tr>
      <td>configuration/views/&lt;id&gt;</td>
      <td>GET</td>
      <td>Retrieve a view configuration from the configuration store.</td>
      <td colspan="1">-</td>
      <td colspan="1">200 OK</td>
      <td colspan="1">
        <span>A view configuration.</span>
      </td>
    </tr>
    <tr>
      <td colspan="1">
        <span>configuration/views/?&lt;options&gt;</span>
      </td>
      <td colspan="1">GET</td>
      <td colspan="1">
        <span>Retrieve a list of view Ids and titles from the configuration store.</span>
      </td>
      <td colspan="1">-</td>
      <td colspan="1">200 OK</td>
      <td colspan="1">List of view Ids.</td>
    </tr>
    <tr>
      <td colspan="1">configuration/views/&lt;id&gt;</td>
      <td colspan="1">PUT</td>
      <td colspan="1">Full update of a view configuration.</td>
      <td colspan="1">
        <span>A view configuration (including Id)</span>
      </td>
      <td colspan="1">200 OK</td>
      <td colspan="1">-</td>
    </tr>
    <tr>
      <td colspan="1">configuration/views</td>
      <td colspan="1">POST</td>
      <td colspan="1">Store a new view configuration.</td>
      <td colspan="1">
        <span>A view configuration without an Id.</span>
      </td>
      <td colspan="1">201 Created</td>
      <td colspan="1">
        <span>A view Id</span>
      </td>
    </tr>
    <tr>
      <td colspan="1">configuration/views/&lt;id&gt;</td>
      <td colspan="1">DELETE</td>
      <td colspan="1">Delete a view configuration.</td>
      <td colspan="1">-</td>
      <td colspan="1">
        <span style="color: rgb(0,0,0);">204 No Content</span>
      </td>
      <td colspan="1">-</td>
    </tr>
  </tbody>
</table>

### Options

#### Filter Options

#### Title option

	<resource>?title=startsWith

Example

	<resource>?title=My

#### User option

	<resource>?author=String

Example

	<resource>?author=eshakespeare

#### Tag option

	<resource>?tag=String

Example

	<resource>?tag=design

#### Paging option

	<resource>?pageNo=Integer&pageSize=Integer

Example

	<resource>?pageNo=1&pageSize=20

#### Ids option

	<resource>?ids=UUID,UUID,UUID,...

Example

	<resource>?ids=120398adb9d781120398adb9d781,081401087aajapife0e3e8f80f3f2b2


#### Output type options

Output type

	<resource>?outputType=full|compact

full - returns entire configuration

compact - is the default format and returns _id and title

Example

	<resource>?outputType=full

Id

The Id is a UUID generated by the server.

## Configuration formats

Configurations formats are defined here [VE Visualization Configuration Formats](visualization_configuration_formats.html).

## List of dashboards or views response

### Response format

	Dashboard list = view list = [Item1, Item2]
	Item = {"title": String, "Id" : UUID}

Example

	{"title": "My dashboard", "Id": "1b167ba0-8f2f-11e3-baa8-0800200c9a66"},
	{"title": "Someone elses other dashboard", "Id" : "40080870-8f2f-11e3-baa8-0800200c9a66"}

### Response codes

<table>
  <tbody>
    <tr>
      <th>Response code</th>
      <th>Description</th>
    </tr>
    <tr>
      <td colspan="1">200 OK</td>
      <td colspan="1">Everything worked.</td>
    </tr>
    <tr>
      <td colspan="1">400 Bad Request</td>
      <td colspan="1">An option was not recognized.</td>
    </tr>
    <tr>
      <td colspan="1">404 Not Found</td>
      <td colspan="1">A resource was not found.</td>
    </tr>
    <tr>
      <td colspan="1">405 Method not allowed</td>
      <td colspan="1">Using a method that is not supported by the resource (POST for the options resource).</td>
    </tr>
    <tr>
      <td colspan="1">409 Conflict</td>
      <td colspan="1">Trying to update a resource where title is the same as for another resource</td>
    </tr>
    <tr>
      <td>500 Internal Server Error</td>
      <td>Some internal error occurred.</td>
    </tr>
  </tbody>
</table>



# Historical Data Service

This REST service provides a way for a client to get historical data. It uses HTTP as underlying layer.

The query handler resource

<table>
  <tbody>
    <tr>
      <th>Resource</th>
      <th>Method</th>
      <th>Description</th>
      <th colspan="1">Request body</th>
      <th colspan="1">Response code on success</th>
      <th colspan="1">Response body<span style="color: rgb(0,0,0);"> on success</span>
      </th>
    </tr>
    <tr>
      <td>historicaldata/queryhandler/&lt;options&gt;</td>
      <td>GET</td>
      <td>Retrieve the historical data for a specific model</td>
      <td colspan="1">-</td>
      <td colspan="1">200 OK</td>
      <td colspan="1">
        <span>A model</span>
      </td>
    </tr>
  </tbody>
</table>


## Model resource

The model resource is the model type. Model types for each model can be found here [VE Model Definitions](model_definitions.html).

### Options

#### The model option

	<resource>?model=modelName

Example

	<resource>?model=directedAcyclicGraph


#### The model version option

	<resource>?modelVersion=String

Example

	<resource>?modelVersion="1.2.3"

#### The query option

	<resource>?query=queryString

The query language is defined here [VE Query language](query_language.html).
 

#### Paging option

	<resource>?pageNo=Integer&pageSize=Integer

Example

	<resource>?pageNo=1&pageSize=20

The pageNo and pageSize options probably have different meaning for different models.

### Model response

Model definitions can be found here  [VE Model Definitions](model_definitions.html).

### Response codes

<table>
  <tbody>
    <tr>
      <th>Response code</th>
      <th>Description</th>
    </tr>
    <tr>
      <td colspan="1">200 OK</td>
      <td colspan="1">Everything worked.</td>
    </tr>
    <tr>
      <td colspan="1">400 Bad Request</td>
      <td colspan="1">An option was not recognized.</td>
    </tr>
    <tr>
      <td colspan="1">404 Not Found</td>
      <td colspan="1">A resource was not found.</td>
    </tr>
    <tr>
      <td colspan="1">405 Method not allowed</td>
      <td colspan="1">Using a method (POST) that is not supported by the resource.</td>
    </tr>
    <tr>
      <td>500 Internal Server Error</td>
      <td>Some internal error occurred.</td>
    </tr>
  </tbody>
</table>


# Live Data Service

This REST service primararly provides a way for a client to subscribe to live data. However, for live views it will also provide the view with initial historical data. It uses websockets as underlying layer when available. That is, we will run REST over webscokets, so to speak. Meaning that we would use the REST (and HTTP) terminology and conventions when possible. Note that the procedure for setting up a websocket connection/session is not described here.


### The subscription resource

We use REST and HTTP terminology but we have specified our own event format and body. That is,our event as specified in the Event format chapter will be sent in the websocket body or, when using socket.io, it could also be sent in the HTTP body if the client does not support websockets.

<table>
  <tbody>
    <tr>
      <th>Resource</th>
      <th colspan="1">Socket.IO Event name</th>
      <th>Method</th>
      <th>Description</th>
      <th colspan="1">Event body</th>
    </tr>
    <tr>
      <td colspan="1">livedata/subscriptions/&lt;id&gt;</td>
      <td colspan="1">
        <span>subscription</span>
      </td>
      <td colspan="1">PUT</td>
      <td colspan="1">Full update of a subscription.</td>
      <td colspan="1">A subscription configuration (including Id)</td>
    </tr>
    <tr>
      <td colspan="1">livedata/subscriptions/&lt;id&gt;</td>
      <td colspan="1">
        <span>subscription</span>
      </td>
      <td colspan="1">PUT</td>
      <td colspan="1">Create a subscription.</td>
      <td colspan="1">A subscription configuration (including Id)</td>
    </tr>
    <tr>
      <td colspan="1">livedata/subscriptions/&lt;id&gt;</td>
      <td colspan="1">
        <span>subscription</span>
      </td>
      <td colspan="1">DELETE</td>
      <td colspan="1">Delete a subscription.</td>
      <td colspan="1">-</td>
    </tr>
    <tr>
      <td colspan="1">
        <span>livedata/subscriptions/&lt;id&gt;</span>
      </td>
      <td colspan="1">update</td>
      <td colspan="1">ASYNC</td>
      <td colspan="1">The server, asynchronously, sends an updated model to the client.</td>
      <td colspan="1">A model</td>
    </tr>
    <tr>
      <td colspan="1">
        <span>livedata/subscriptions/&lt;id&gt;</span>
      </td>
      <td colspan="1">failure</td>
      <td colspan="1">ASYNC</td>
      <td colspan="1">The server has detected an error when handling a request from the client</td>
      <td colspan="1">Erroc code</td>
    </tr>
  </tbody>
</table>


### Event format

The definition of the event format.

	Event = {
			"method" : "PUT" | "DELETE" | "ASYNC",
			"eventURI" : "ve:livedata/subscriptions/<id>",
			"version" : "1.0",
			"eventBody" : body
			}

Example

	Event = {
			"method" : "PUT",
			"eventURI" : "ve:livedata/subscriptions/e098b740-9348-11e3-baa8-0800200c9a66",
			"version" : "1.0",
			"eventBody" : body
			}

### Id

The Id is a UUID generated by the client.

### Model event body

	eventBody = {
			"model" : model
		}

Model definitions can be found here  [VE Model Definitions](model_definitions.html).

### Subscription configuration event body

	eventBody = {
			"model" : String
			"modelVersion" : String
			"query" : String
			"queryOptions" {Object1, Object2, ...}
			"updateInterval" : Integer // In seconds
		}

Example

	eventBody = {
			"model" : "matrix",
			"modelVersion" : "1.0",
			"query" : "eventType=EiffelConfidenceLevelModifiedEvent",
			"queryOptions" : {"pageSize":100,"pageNo":1},
			"updateInterval" : 15
		}

### Error event body

	eventBody = {
			"code" : Integer,
			"error" : String
		}

Example

	eventBody = {
			"code" : 400,
			"error" : "Bad Request"
		}

### Response codes

<table>
  <tbody>
    <tr>
      <th>Response code</th>
      <th>Description</th>
    </tr>
    <tr>
      <td colspan="1">200 OK</td>
      <td colspan="1">Everything worked.</td>
    </tr>
    <tr>
      <td colspan="1">400 Bad Request</td>
      <td colspan="1">An option was not recognized.</td>
    </tr>
    <tr>
      <td colspan="1">404 Not Found</td>
      <td colspan="1">A resource was not found.</td>
    </tr>
    <tr>
      <td colspan="1">405 Method not allowed</td>
      <td colspan="1">Using a method (POST) that is not supported by the resource.</td>
    </tr>
    <tr>
      <td>500 Internal Server Error</td>
      <td>Some internal error occurred.</td>
    </tr>
  </tbody>
</table>

