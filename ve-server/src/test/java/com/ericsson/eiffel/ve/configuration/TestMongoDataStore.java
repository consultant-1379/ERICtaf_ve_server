package com.ericsson.eiffel.ve.configuration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.mock;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.configuration.MongoDataStore;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ObjectId.class})
public class TestMongoDataStore {
	private MongoDataStore unitUnderTest;
	private ProducerTemplate producerTemplate;
	private static String id = "1";
	private JsonParser parser;


	@Before
	public void setUp() throws Exception {
		VETestSupport.setupLogging();
		ObjectId objectId = mock(ObjectId.class);
		when(objectId.toString()).thenReturn(id);
		PowerMockito.mockStatic(ObjectId.class);
		mock(ObjectId.class);
		when(ObjectId.get()).thenReturn(objectId);
				
		producerTemplate = mock(ProducerTemplate.class);
		parser = new JsonParser();
		unitUnderTest = new MongoDataStore("localhost");
		unitUnderTest.producerTemplate = producerTemplate;
	}

	@Test
	public void testGetDashboard(){
		String jsonInput = "{'_id':'"+id+"', 'title':'TITLE', 'data':30}";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		when(producerTemplate.requestBody("direct:findDashboardById", id, DBObject.class)).thenReturn(dbObject);
		String jsonOutput =  unitUnderTest.getDashboard(id);
		assertEquals(parser.parse(jsonInput), parser.parse(jsonOutput));
	}
	
	@Test
	public void testGetDashboardNotFound(){
		DBObject dbObject = null;
		when(producerTemplate.requestBody("direct:findDashboardById", id, DBObject.class)).thenReturn(dbObject);
		String jsonOutput = unitUnderTest.getDashboard(id);
		verify(producerTemplate).requestBody("direct:findDashboardById", id, DBObject.class);
		assertNull(jsonOutput);
	}
	
	@Test
	public void testGetDashboards(){
		Map<String,String> options = new HashMap<String,String>();
		String jsonInput = "[{'_id':'"+id+"', 'title':'TITLE', 'data':30},{'_id':'"+id+1+"', 'title':'TITLE', 'data':31}]";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		when(producerTemplate.requestBodyAndHeader(eq("direct:findAllDashboards"), eq(null), eq(MongoDbConstants.FIELDS_FILTER), anyObject())).thenReturn(dbObject);
		String jsonOutput = unitUnderTest.getDashboards(options);
		assertEquals(parser.parse(jsonInput), parser.parse(jsonOutput));
	}
	
	@Test
	public void testGetDashboardsEmpty(){
		Map<String,String> options = new HashMap<String,String>();
		DBObject dbObject = (DBObject) null;
		when(producerTemplate.requestBodyAndHeader(eq("direct:findAllDashboards"), eq(null), eq(MongoDbConstants.FIELDS_FILTER), anyObject())).thenReturn(dbObject);
		String jsonOutput = unitUnderTest.getDashboards(options);
		assertEquals(parser.parse("[]"), parser.parse(jsonOutput));
	}

//	@Test
//	public void testGetDashboardsFilter(){
//		Map<String,String> options = new HashMap<String,String>();
//		options.put("ids", "1,2,3");
//		String jsonInput = "[{'_id':'"+id+"', 'title':'TITLE', 'data':30},{'_id':'"+id+1+"', 'title':'TITLE', 'data':31}]";
//		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
//		when(producerTemplate.requestBodyAndHeader(eq("direct:findAllDashboards"), eq(null), eq(MongoDbConstants.FIELDS_FILTER), anyObject())).thenReturn(dbObject);
//		String jsonOutput = unitUnderTest.getDashboards(options);
//		assertEquals(parser.parse(jsonInput), parser.parse(jsonOutput));
//	}


	@Test
	public void testDeleteDashboard() throws Exception {
		String jsonInput = "{'_id':'"+id+"', 'title':'TITLE', 'data':30}";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		when(producerTemplate.requestBody("direct:findDashboardById", id, DBObject.class)).thenReturn(dbObject);

		DBObject body = new BasicDBObject("_id", id);
		DBObject dbObjectDeleteResp = (DBObject) JSON.parse("{}");
		when(producerTemplate.requestBody("direct:removeDashboardById", body)).thenReturn(dbObjectDeleteResp);
		assertTrue(unitUnderTest.deleteDashboard(id));   
	}

	@Test
	public void testDeleteDashboardNotFound() throws Exception {
		DBObject body = new BasicDBObject("_id", id);
		DBObject dbObjectDeleteResp = (DBObject) JSON.parse("{}");
		when(producerTemplate.requestBody("direct:removeDashboardById", body)).thenReturn(dbObjectDeleteResp);
		assertFalse(unitUnderTest.deleteDashboard(id));   
	}

	@Test
	public void testPutDashboard() throws Exception {
		String jsonInput = "{'_id':'"+id+"', 'title':'TITLE', 'data':30}";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		when(producerTemplate.requestBody("direct:findDashboardById", id, DBObject.class)).thenReturn(dbObject);

		when(producerTemplate.requestBody("direct:storeDashboard", jsonInput, DBObject.class)).thenReturn(dbObject);

		assertFalse(unitUnderTest.putDashboard(jsonInput).isEmpty());
		verify(producerTemplate).requestBody("direct:storeDashboard", jsonInput, DBObject.class);
	}
	
	@Test
	public void testPutDashboardIdNotFond() throws Exception {
		String jsonInput = "{'_id':'"+id+"', 'title':'TITLE', 'data':30}";
		DBObject dbObject = null;
		when(producerTemplate.requestBody("direct:findDashboardById", id, DBObject.class)).thenReturn(dbObject);

		assertTrue(unitUnderTest.putDashboard(jsonInput).isEmpty());
	}

	@Test
	public void testPostDashboard() throws Exception {
		String jsonInput = "{}";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		String json = "{title:TITLE,data:\"D3\"}";
		String jsonStore = "{\"title\":\"TITLE\",\"data\":\"D3\",\"_id\":\""+id+"\"}";
		when(producerTemplate.requestBody(eq("direct:storeDashboard"), eq(jsonStore), eq(DBObject.class))).thenReturn(dbObject);

		unitUnderTest.postDashboard(json);
		verify(producerTemplate).requestBody("direct:storeDashboard", jsonStore, DBObject.class);
	}

	
	
//	#### VIEWS

	@Test
	public void testGetView(){
		String jsonInput = "{'_id':'"+id+"', 'title':'TITLE', 'data':30}";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		when(producerTemplate.requestBody("direct:findViewById", id, DBObject.class)).thenReturn(dbObject);
		String jsonOutput =  unitUnderTest.getView(id);
		assertEquals(parser.parse(jsonInput), parser.parse(jsonOutput));
	}
	
	@Test
	public void testGetViewNotFound(){
		DBObject dbObject = null;
		when(producerTemplate.requestBody("direct:findViewById", id, DBObject.class)).thenReturn(dbObject);
		String jsonOutput = unitUnderTest.getView(id);
		verify(producerTemplate).requestBody("direct:findViewById", id, DBObject.class);
		assertNull(jsonOutput);
	}
	
	@Test
	public void testGetViews(){
		Map<String,String> options = new HashMap<String,String>();
		String jsonInput = "[{'_id':'"+id+"', 'title':'TITLE', 'data':30},{'_id':'"+id+1+"', 'title':'TITLE', 'data':31}]";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		when(producerTemplate.requestBodyAndHeader(eq("direct:findAllViews"), eq(null), eq(MongoDbConstants.FIELDS_FILTER), anyObject())).thenReturn(dbObject);
		String jsonOutput = unitUnderTest.getViews(options);
		assertEquals(parser.parse(jsonInput), parser.parse(jsonOutput));
	}
	
	@Test
	public void testGetViewEmpty(){
		Map<String,String> options = new HashMap<String,String>();
		DBObject dbObject = (DBObject) null;
		when(producerTemplate.requestBodyAndHeader(eq("direct:findAllViews"), eq(null), eq(MongoDbConstants.FIELDS_FILTER), anyObject())).thenReturn(dbObject);
		String jsonOutput = unitUnderTest.getViews(options);
		assertEquals(parser.parse("[]"), parser.parse(jsonOutput));
	}

//	@Test
//	public void testGetDashboardsFilter(){
//		Map<String,String> options = new HashMap<String,String>();
//		options.put("ids", "1,2,3");
//		String jsonInput = "[{'_id':'"+id+"', 'title':'TITLE', 'data':30},{'_id':'"+id+1+"', 'title':'TITLE', 'data':31}]";
//		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
//		when(producerTemplate.requestBodyAndHeader(eq("direct:findAllDashboards"), eq(null), eq(MongoDbConstants.FIELDS_FILTER), anyObject())).thenReturn(dbObject);
//		String jsonOutput = unitUnderTest.getDashboards(options);
//		assertEquals(parser.parse(jsonInput), parser.parse(jsonOutput));
//	}


	@Test
	public void testDeleteView() throws Exception {
		String jsonInput = "{'_id':'"+id+"', 'title':'TITLE', 'data':30}";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		when(producerTemplate.requestBody("direct:findViewById", id, DBObject.class)).thenReturn(dbObject);

		DBObject body = new BasicDBObject("_id", id);
		DBObject dbObjectDeleteResp = (DBObject) JSON.parse("{}");
		when(producerTemplate.requestBody("direct:removeViewById", body)).thenReturn(dbObjectDeleteResp);
		assertTrue(unitUnderTest.deleteView(id));   
	}

	@Test
	public void testDeleteViewNotFound() throws Exception {
		DBObject body = new BasicDBObject("_id", id);
		DBObject dbObjectDeleteResp = (DBObject) JSON.parse("{}");
		when(producerTemplate.requestBody("direct:removeViewById", body)).thenReturn(dbObjectDeleteResp);
		assertFalse(unitUnderTest.deleteView(id));   
	}

	@Test
	public void testPutView() throws Exception {
		String jsonInput = "{'_id':'"+id+"', 'title':'TITLE', 'data':30}";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		when(producerTemplate.requestBody("direct:findViewById", id, DBObject.class)).thenReturn(dbObject);

		when(producerTemplate.requestBody("direct:storeView", jsonInput, DBObject.class)).thenReturn(dbObject);

		assertFalse(unitUnderTest.putView(jsonInput).isEmpty());
		verify(producerTemplate).requestBody("direct:storeView", jsonInput, DBObject.class);
	}
	
	@Test
	public void testPutViewIdNotFond() throws Exception {
		String jsonInput = "{'_id':'"+id+"', 'title':'TITLE', 'data':30}";
		DBObject dbObject = null;
		when(producerTemplate.requestBody("direct:findViewById", id, DBObject.class)).thenReturn(dbObject);

		assertTrue(unitUnderTest.putView(jsonInput).isEmpty());
	}

	@Test
	public void testPostView() throws Exception {
		String jsonInput = "{}";
		DBObject dbObject = (DBObject) JSON.parse(jsonInput);
		String json = "{title:TITLE,data:\"D3\"}";
		String jsonStore = "{\"title\":\"TITLE\",\"data\":\"D3\",\"_id\":\""+id+"\"}";
		when(producerTemplate.requestBody(eq("direct:storeView"), eq(jsonStore), eq(DBObject.class))).thenReturn(dbObject);

		unitUnderTest.postView(json);
		verify(producerTemplate).requestBody("direct:storeView", jsonStore, DBObject.class);
	}


}
