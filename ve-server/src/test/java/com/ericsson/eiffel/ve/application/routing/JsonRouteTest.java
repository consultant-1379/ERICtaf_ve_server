package com.ericsson.eiffel.ve.application.routing;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffel.ve.VETestSupport;
import com.ericsson.eiffel.ve.application.routing.JsonRoute;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonRouteTest {

	@Before
	public void setUp() throws Exception{
		VETestSupport.setupLogging();
	}
	
    @Test
    public void shouldMatch() throws Exception {
        JsonRoute routeWithValue = JsonRoute.parse("a.b:3");

        assertTrue(routeWithValue.matches("{\"a\":{\"b\":3}}"));
        assertFalse(routeWithValue.matches("{\"a\":{\"b\":4}}"));

        JsonRoute routeWithoutValue = JsonRoute.parse("a.b");

        assertTrue(routeWithoutValue.matches("{\"a\":{\"b\":4}}"));
    }
}
