package com.xebialabs.overcast;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesLoader {

    private static Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    public static final String OVERCAST_PROPERTY_FILE = "overcast.properties";


    public static Properties loadOvercastProperties() {
        try {
            Properties properties = new Properties();
            loadOvercastPropertiesFromClasspath(properties);
            loadOvercastPropertiesFromHomeDirectory(properties);
            loadOvercastPropertiesFromCurrentDirectory(properties);
            return properties;
        } catch (IOException exc) {
            throw new RuntimeException("Cannot load " + OVERCAST_PROPERTY_FILE, exc);
        }
    }


    private static void loadOvercastPropertiesFromClasspath(final Properties properties) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(OVERCAST_PROPERTY_FILE);
        if (resource != null) {
            InputStream in = resource.openStream();
            try {
                logger.info("Loading {}", resource);
                properties.load(in);
            } finally {
                in.close();
            }
        } else {
            logger.warn("File {} not found on classpath.", OVERCAST_PROPERTY_FILE);
        }
    }

    private static void loadOvercastPropertiesFromCurrentDirectory(final Properties properties) throws FileNotFoundException, IOException {
        loadOvercastPropertiesFromFile(new File(OVERCAST_PROPERTY_FILE), properties);
    }

    private static void loadOvercastPropertiesFromFile(File file, Properties properties) throws FileNotFoundException, IOException {
        if (file.exists()) {
            FileInputStream in = new FileInputStream(file);
            try {
                logger.info("Loading {}", file.getAbsolutePath());
                properties.load(in);
            } finally {
                in.close();
            }
        } else {
            logger.warn("File {} not found.", file.getAbsolutePath());
        }
    }

    private static void loadOvercastPropertiesFromHomeDirectory(final Properties properties) throws FileNotFoundException, IOException {
        loadOvercastPropertiesFromFile(new File(System.getProperty("user.home"), ".overcast/" + OVERCAST_PROPERTY_FILE), properties);
    }

}



