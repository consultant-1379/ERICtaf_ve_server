package com.ericsson.eiffel.ve.infrastructure.config;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SettingsProviderTest {

	@Before
	public void setUp() {

	}
	
	/**
	 * Test that loadSettings() returns default file from command line argument
	 * if given.
	 */
	@Test
	public void loadSettingsReturnsProvidedValueTest() {
		String filepath = "src/test/resources/settings.yml"; 
		System.setProperty("eiffel.veserver.env", Environment.DEVELOPMENT);
		SettingsProvider sp = new SettingsProvider(filepath);
		Settings actualSetting = sp.loadSettings();
		assertEquals("http://localhost:8090/eventrepository/restapi", actualSetting.getString("eventRepository.uri"));
	}
	
}

