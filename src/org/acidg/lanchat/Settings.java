package org.acidg.lanchat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manages user settings and persists them in a settings file in the users
 * appdata or home directory.
 */
public class Settings {
	private static final String PROPERTIES_FILE_LOCATION = getPropertiesLocation();
	private static final Logger LOGGER = Logger.getLogger(Settings.class.getName());
	private static final String BROADCASTING_PORT_KEY = "broadcasting_port";
	private static final String USERNAME_KEY = "username";
	private static final Integer DEFAULT_PORT = 10420;
	private static final String DEFAULT_USERNAME = "Unknown User";
	private static final String PROPERTIES_FILE_COMMENT = "Settings for Lanchat";
	public static final Settings INSTANCE = new Settings();

	private Properties properties;

	private Settings() {
		LOGGER.info("Using settings file: " + PROPERTIES_FILE_LOCATION);
		properties = new Properties();
		File propertiesFile = new File(PROPERTIES_FILE_LOCATION);

		try {
			if (!propertiesFile.exists()) {
				createNewDefaultProperties(propertiesFile);
			}
			properties.load(new FileReader(propertiesFile));
		} catch (IOException e) {
			LOGGER.warning("Could not load properties! Using defaults " + e.getMessage());
		}
	}

	private void createNewDefaultProperties(File propertiesFile) {
		try {
			LOGGER.info("Creating new properties file");
			Properties defaultProperties = new Properties();
			defaultProperties.put(BROADCASTING_PORT_KEY, DEFAULT_PORT.toString());
			defaultProperties.put(USERNAME_KEY, DEFAULT_USERNAME);
			
			propertiesFile.getParentFile().mkdirs();
			defaultProperties.store(new FileWriter(propertiesFile), PROPERTIES_FILE_COMMENT);
		} catch (IOException e) {
			LOGGER.warning("Could not create properties file: " + e.getMessage());
		}
	}

	public int getBroadcastingPort() {
		String portString = properties.getProperty(BROADCASTING_PORT_KEY);
		if (portString == null) {
			properties.put(BROADCASTING_PORT_KEY, DEFAULT_PORT);
			portString = DEFAULT_PORT.toString();
		}
		
		return Integer.parseInt(portString);
	}

	public void setPort(int port) {
		properties.put(BROADCASTING_PORT_KEY, port);
		persist();
	}

	public String getUsername() {
		return properties.getProperty(USERNAME_KEY, DEFAULT_USERNAME);
	}

	public void setUsername(String username) {
		properties.put(USERNAME_KEY, username);
		persist();
	}

	private void persist() {
		File propertiesFile = new File(PROPERTIES_FILE_LOCATION);
		try {
			properties.store(new FileWriter(propertiesFile), PROPERTIES_FILE_COMMENT);
		} catch (IOException e) {
			LOGGER.warning("Could not write to file " + PROPERTIES_FILE_LOCATION + ": " + e.getMessage());
		}
	}

	private static String getPropertiesLocation() {
		String osString = System.getProperty("os.name").toLowerCase();
		if (osString.contains("win")) {
			return System.getenv("APPDATA") + File.separator + "Lanchat" + File.separator + "lanchat.properties";
		}
		return System.getProperty("user.home") + File.separator + ".lanchat.conf";
	}
}
