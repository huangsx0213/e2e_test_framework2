package com.example.api.config;

import java.io.InputStream;
import java.util.Properties;

public class EnvironmentConfig {
    private static final String CONFIG_FILE = "/config/env-config.properties";
    private static Properties properties;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = EnvironmentConfig.class.getResourceAsStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load environment configuration", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}