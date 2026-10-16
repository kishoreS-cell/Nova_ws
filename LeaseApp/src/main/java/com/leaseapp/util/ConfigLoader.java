package com.leaseapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();
    private static boolean loaded = false;

    private static synchronized void loadIfNeeded() {
        if (loaded) return;
        try (InputStream in = ConfigLoader.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new RuntimeException(
                    "config.properties not found on classpath. " +
                    "Create it at src/main/resources/config.properties (see config.properties.example)."
                );
            }
            props.load(in);
            loaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        loadIfNeeded();
        String value = props.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Missing config key: " + key);
        }
        return value;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}