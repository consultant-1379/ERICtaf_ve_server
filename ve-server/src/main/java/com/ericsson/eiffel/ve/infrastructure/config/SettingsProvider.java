package com.ericsson.eiffel.ve.infrastructure.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.ericsson.duraci.logging.EiffelLog;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;

/**
 *
 */
public final class SettingsProvider {

    public static final String SETTINGS_LOCATION = "settings.yml";
    public static final String DEFAULT_ENV = Environment.PRODUCTION;
    public static final String ENV_PROPERTY = "eiffel.veserver.env";
    private String settingsLocation = "";
    
    private static final EiffelLog logger = new JavaLoggerEiffelLog(SettingsProvider.class);


    Settings settings;

	public SettingsProvider(String settingsLocation) {
		this.settingsLocation = settingsLocation;
	}
    
    public Settings loadSettings() {
        if (settings == null) {
            settings = getSettings();
        }
        return settings;
    }

    private Settings getSettings() {
        try {
        	Reader reader = getReader(settingsLocation);
        	String environment = determineEnvironment();
            logger.info("Loading settings for environment : " + environment);
            return readYaml(environment, reader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + settingsLocation, e);
        }
    }

    private String determineEnvironment() {
        String environment = System.getProperty(ENV_PROPERTY);
        if (environment == null) {
            environment = DEFAULT_ENV;
        }
        return environment;
    }

    private Settings readYaml(String environment, Reader reader) {
        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> result = (Map<String, Map<String, String>>) yaml.load(reader);
        Map<String, String> properties;
        if (result.containsKey(environment)) {
            properties = result.get(environment);
        } else {
            properties = result.get(DEFAULT_ENV);
        }
        return new Settings(properties);
    }
    
    /**
	 * @param filepath Path to file.
	 * @return A Reader referring to the file.
	 * @throws IOException
	 */
	public Reader getReader(String filepath) throws IOException {
		File f = new File(filepath);
		if (!f.exists() || !f.canRead()){
			System.out.println("SettingsProvider.getReader() File " + filepath + " does not exist or is not readable");
		}
		return new FileReader(filepath);
	}
	
}
