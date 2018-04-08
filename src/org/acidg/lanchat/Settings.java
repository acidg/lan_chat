package org.acidg.lanchat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class Settings {
	private static final String PROPERTIES_FILE_LOCATION = getPropertiesLocation();
	private static final Logger LOGGER = Logger.getLogger(Settings.class.getName());
	public static final Settings INSTANCE = new Settings();
	private static final String BROADCASTING_PORT_KEY = "broadcasting_port";
	private static final String USERNAME_KEY = "username";
	private static final Integer DEFAULT_PORT = 11002;
	
	private Properties properties;
	
	private Settings() {
		properties = new Properties();
		File propertiesFile = new File(PROPERTIES_FILE_LOCATION);
		
		try {
			propertiesFile.createNewFile();
			properties.load(new FileReader(propertiesFile));
		} catch (IOException e) {
			LOGGER.warning("Could not load properties! Using defaults " + e.getMessage());
		}
	}
	
	public int getPort() {
		return Integer.parseInt(properties.getProperty(BROADCASTING_PORT_KEY, DEFAULT_PORT.toString()));
	}
	
	public void setPort(int port) {
		properties.put(BROADCASTING_PORT_KEY, port);
		persist();
	}
	
	public String getUsername() {
		return properties.getProperty(USERNAME_KEY, "new_user");
	}

	public void setUsername(String username) {
		properties.put(USERNAME_KEY, username);
		persist();
	}
	
	private void persist() {
		File propertiesFile = new File(PROPERTIES_FILE_LOCATION);
		try {
			properties.store(new FileWriter(propertiesFile), "Settings for Lanchat");
		} catch (IOException e) {
			LOGGER.warning("Could not write to file " + PROPERTIES_FILE_LOCATION + ": " + e.getMessage());
		}
	}

	private static String getPropertiesLocation() {
		String osString = System.getProperty("os.name").toLowerCase();
		if (osString.contains("win")) {
			return System.getProperty("APPDATA") + File.separator + "Lanchat" + File.separator + "lanchat.properties";
		}
		return System.getProperty("user.home") + File.separator + ".lanchat.conf";
	}
}
