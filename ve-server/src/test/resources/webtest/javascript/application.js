$(function () {
    "use strict";

    var detect = $('#detect');
    var header = $('#header');
    var content = $('#content');
    var directedAcyclicContent = $('#directedAcyclicContent');
	var ratioContent = $('#ratioContent');
	var eventContent = $('#eventContent');
    var directedAcyclicSubscribeButton = $('#directedAcyclicSubscribeButton');
    var directedAcyclicUnsubscribeButton = $('#directedAcyclicUnsubscribeButton');
    var ratioSubscribeButton = $('#ratioSubscribeButton');
    var ratioUnsubscribeButton = $('#ratioUnsubscribeButton');
    var eventSubscribeButton = $('#eventSubscribeButton');
    var eventUnsubscribeButton = $('#eventUnsubscribeButton');
    var historicalButton = $('#historicalButton');
    var status = $('#status');
    var myName = false;
    var author = null;
    var logged = false;
    var loginurl = "";
    var pathname = document.location.pathname;
    var lastdot = pathname.lastIndexOf("/");
    if (lastdot > 1) {
        loginurl = pathname.substr(1, lastdot);
    }
	
	var contentToIdMapping = { 'directedAcyclicContent' : generateUUID(),'ratioContent' : generateUUID(), 'eventContent' : generateUUID(), 'dagContent' : generateUUID() };
    var socket = io.connect('', {'host':'localhost', 'port':'8585', 'resource':loginurl + 'socket.io'});

    socket.on('connect', function () {
        content.html($('<p>', { text: 'Atmosphere connected using ' + this.socket.transport.name}));
        eventSubscribeButton.removeAttr('disabled');
        eventUnsubscribeButton.attr('disabled', 'disabled');
        ratioSubscribeButton.removeAttr('disabled');
        ratioUnsubscribeButton.attr('disabled', 'disabled');
        historicalButton.removeAttr('disabled');
        status.text('Subscribe to messages:');
        directedAcyclicSubscribeButton.removeAttr('disabled');
        directedAcyclicUnsubscribeButton.attr('disabled', 'disabled');

        $.each(this.socket.transports, function(index, item) {
            $("#transport").append(new Option(item, item));
        });
    });

    socket.on('update', message);

    socket.on('error', function (e) {
        content.html($('<p>', { text: 'Sorry, but there\'s some problem with your '
            + 'socket or the server is down' }));
    });
    
    directedAcyclicSubscribeButton.on('click', function() {
			socket.emit('event', generateMessage("PUT",contentToIdMapping['directedAcyclicContent'],"domainId=VEDomain","DirectedAcyclicGraphModel",5));
			$(this).val('');

			directedAcyclicSubscribeButton.attr('disabled', 'disabled');
			directedAcyclicUnsubscribeButton.removeAttr('disabled');
    });

    directedAcyclicUnsubscribeButton.on('click', function() {
    		socket.emit('event', generateMessage("DELETE",contentToIdMapping['directedAcyclicContent'],"eventType","DirectedAcyclicGraphModel",5));
    		$(this).val('');

			directedAcyclicUnsubscribeButton.attr('disabled', 'disabled');
			directedAcyclicSubscribeButton.removeAttr('disabled');
    });
    
    ratioSubscribeButton.on('click', function() {
			socket.emit('event', generateMessage("PUT",contentToIdMapping['ratioContent'],"eventType","RatioDistributionModel",5));
            $(this).val('');

            ratioSubscribeButton.attr('disabled', 'disabled');
			ratioUnsubscribeButton.removeAttr('disabled');
    });

    ratioUnsubscribeButton.on('click', function() {
			socket.emit('event', generateMessage("DELETE",contentToIdMapping['ratioContent'],"eventType","RatioDistributionModel",5));
            $(this).val('');

            ratioUnsubscribeButton.attr('disabled', 'disabled');
			ratioSubscribeButton.removeAttr('disabled');
    });

    eventSubscribeButton.on('click', function() {
			socket.emit('event', generateMessage("PUT",contentToIdMapping['eventContent'],"eventType","EventModel",5));
            $(this).val('');

            eventSubscribeButton.attr('disabled', 'disabled');
			eventUnsubscribeButton.removeAttr('disabled');
    });

    eventUnsubscribeButton.on('click', function() {
			socket.emit('event', generateMessage("DELETE",contentToIdMapping['eventContent'],"eventType","EventModel",5));
            $(this).val('');

            eventUnsubscribeButton.attr('disabled', 'disabled');
			eventSubscribeButton.removeAttr('disabled');
    });
    
    historicalButton.on('click', function() {
    	socket.emit('historicaldata', '{"filters" : [{"key":"eventId", "comp":"=", "val":"11d265f8-8be9-4a90-9dc6-69776668d56e"}]}');
    	$(this).val('');
    });

    function message(msg) {
		if(msg != null) {
			if(msg['eventBody']['modelMetaData']['Type'] === 'ratioDistribution')
				ratioContent.html('<p>{<br>' + objectToHtml(msg, "&nbsp;&nbsp;") + '}</p>');
			else if(msg['eventBody']['modelMetaData']['Type'] === 'event')
				eventContent.html('<p>{<br>' + objectToHtml(msg, "&nbsp;&nbsp;") + '}</p>');
			else if(msg['eventBody']['modelMetaData']['Type'] === 'directedAcyclicGraphModel')
				directedAcyclicContent.html('<p>{<br>' + objectToHtml(msg, "&nbsp;&nbsp;") + '}</p>');
		}
    }
	
	function generateMessage(method, id, query, model, updateInterval) {
		var messageStr = '{"method" : "'+method+'", "eventURI" : "ve:livedata/subscriptions/'+id+'", "version" : "1.0", "eventBody" : ';
		messageStr += '{"query" : "'+query+'", "queryOptions" : {"type" : "flowChart","base" : "eventType","information" : ["domainId","eventData.resultCode"],"includeConnections" : true,"dagAggregation" : false,"maxNumberOfDags" : 10,"pageSize" : "all","pageNo" : 1,"title" : "eventType","startDate" : "2013-02-25", "endDate" : "2014-03-25"}, "model" : "'+model+'", "modelVersion" : "1.0", "updateInterval" : '+updateInterval+'}';
		messageStr += '}';
		return messageStr;
	}
	
	function generateUUID(){
		var d = Date.now(); //new Date().getTime();
		var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			var r = (d + Math.random()*16)%16 | 0;
			d = Math.floor(d/16);
			return (c=='x' ? r : (r&0x7|0x8)).toString(16);
		});
		return uuid;
	};
	
	function objectToHtml(obj, prefix) {
		var htmlStr = "";
		for(var variable in obj) {
			if(Object.prototype.toString.call(obj[variable]) === "[object Object]") {
				htmlStr += prefix + variable + ": {<br/>";
				htmlStr += objectToHtml(obj[variable], prefix + '&nbsp;&nbsp');
				htmlStr += prefix + "}<br/>";
			}
			else if(Object.prototype.toString.call(obj[variable]) === "[object Array]") {
				htmlStr += prefix + variable + ": [<br/>";
			    htmlStr += arrayToHtml(obj[variable], prefix + '&nbsp;&nbsp');
				htmlStr += prefix + "]<br/>";
			}
			else {
				htmlStr += prefix + variable + ":" + obj[variable] + "<br/>";
			}
		}
		return htmlStr;
	}
	
	function arrayToHtml(ary, prefix) {
		var htmlStr = "";
		for(var index = 0; index < ary.length; index++) {
			if(Object.prototype.toString.call(ary[index]) === "[object Object]") {
				htmlStr += prefix + "{<br/>";
				htmlStr += objectToHtml(ary[index], prefix + '&nbsp;&nbsp');
				htmlStr += prefix + "}<br/>";
			}
			else if(Object.prototype.toString.call(ary[index]) === "[object Array]") {
				htmlStr += prefix + "[<br/>";
			    htmlStr += arrayToHtml(ary[index], prefix + '&nbsp;&nbsp');
				htmlStr += prefix + "]<br/>";
			}
			else {
				htmlStr += prefix + ary[index] + "<br/>";
			}
		}
		return htmlStr;
	}
});

