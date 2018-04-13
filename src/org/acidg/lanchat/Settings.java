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
	private static final String CUSTOM_MESSAGE_KEY = "custom_message";
	private static final String DEFAULT_CUSTOM_MESSAGE = "custom_message";
	private static final String SHOW_ONSTARTUP_KEY = "show_on_startup";
	private static final Boolean DEFAULT_SHOW_ONSTARTUP = true;

	private Properties properties;

	private Settings() {
		LOGGER.info("Using settings file: " + PROPERTIES_FILE_LOCATION);
		properties = new Properties();
		File propertiesFile = new File(PROPERTIES_FILE_LOCATION);

		try {
			if (!propertiesFile.exists()) {
				propertiesFile.getParentFile().mkdirs();
				properties.store(new FileWriter(propertiesFile), PROPERTIES_FILE_COMMENT);
			}
			
			properties.load(new FileReader(propertiesFile));
		} catch (IOException e) {
			LOGGER.warning("Could not load properties! Using defaults " + e.getMessage());
		}
	}

	public int getBroadcastingPort() {
		String portString = properties.getProperty(BROADCASTING_PORT_KEY);
		if (portString == null) {
			properties.put(BROADCASTING_PORT_KEY, DEFAULT_PORT.toString());
			persist();
			portString = DEFAULT_PORT.toString();
		}
		
		return Integer.parseInt(portString);
	}

	public String getUsername() {
		String username = properties.getProperty(USERNAME_KEY);
		if (username == null) {
			properties.put(USERNAME_KEY, DEFAULT_USERNAME);
			persist();
			username = DEFAULT_USERNAME;
		}
		return username;
	}

	public String getCustomMessage() {
		String customMessage = properties.getProperty(CUSTOM_MESSAGE_KEY);
		if (customMessage == null) {
			properties.put(CUSTOM_MESSAGE_KEY, DEFAULT_CUSTOM_MESSAGE);
			persist();
			customMessage = DEFAULT_CUSTOM_MESSAGE;
		}
		return customMessage;
	}

	public boolean getShowOnStartup() {
		String showOnStartupString = properties.getProperty(SHOW_ONSTARTUP_KEY);
		if (showOnStartupString == null) {
			properties.put(SHOW_ONSTARTUP_KEY, DEFAULT_SHOW_ONSTARTUP.toString());
			persist();
			showOnStartupString = DEFAULT_SHOW_ONSTARTUP.toString();
		}
		return Boolean.parseBoolean(showOnStartupString);
	}
	
	public void setPort(int port) {
		properties.put(BROADCASTING_PORT_KEY, port);
		persist();
	}

	public void setUsername(String username) {
		properties.put(USERNAME_KEY, username);
		persist();
	}
	
	public void setCustomMessage(String customMessage) {
		properties.put(CUSTOM_MESSAGE_KEY, customMessage);
		persist();
	}
	
	public void setShowOnStartup(boolean showOnStartup) {
		properties.put(SHOW_ONSTARTUP_KEY, showOnStartup);
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
