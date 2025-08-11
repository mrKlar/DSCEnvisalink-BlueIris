package com.envisalink.blueiris;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Logger logger = LogManager.getLogger(Config.class);
    private final Properties properties = new Properties();

    public Config(String path) {
        loadProperties(path);
    }

    private void loadProperties(String path) {
        try (InputStream input = new FileInputStream(path)) {
            properties.load(input);
            logger.info("Configuration loaded successfully from {}", path);
        } catch (IOException ex) {
            logger.error("Sorry, unable to find or read " + path, ex);
            // Exit if config is not found, as the application cannot run without it.
            System.exit(1);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer for property '{}'. Using default value {}.", key, defaultValue);
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
