package com.xebialabs.overcast;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import static com.google.common.base.Joiner.on;
import static com.google.common.io.Files.readLines;
import static java.nio.charset.Charset.defaultCharset;

public class PropertiesLoader {

    private static Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    public static final String OVERCAST_PROPERTY_FILE = "overcast.properties";
    public static final String OVERCAST_CONF_FILE = "overcast.conf";
    public static final String OVERCAST_USER_DIR = ".overcast";

    private static String getUserOvercastPropertiesPath() {
        return new File(new File(System.getProperty("user.home"), OVERCAST_USER_DIR), OVERCAST_PROPERTY_FILE).getAbsolutePath();
    }

    private static String getUserOvercastConfPath() {
        return new File(new File(System.getProperty("user.home"), OVERCAST_USER_DIR), OVERCAST_CONF_FILE).getAbsolutePath();
    }

    public static Properties loadOvercastProperties() {
        Properties properties = new Properties();
        insertConfigIntoProperties(loadOvercastConfig(), properties);
        return properties;
    }

    public static Config loadOvercastConfig() {
        return loadOvercastConfigFromFile(getUserOvercastConfPath())
                .withFallback(loadOvercastConfigFromFile(getUserOvercastPropertiesPath()))
                .withFallback(loadOvercastConfigFromFile(OVERCAST_CONF_FILE))
                .withFallback(loadOvercastConfigFromFile(OVERCAST_PROPERTY_FILE))
                .withFallback(loadOvercastConfigFromClasspath(OVERCAST_CONF_FILE))
                .withFallback(loadOvercastConfigFromClasspath(OVERCAST_PROPERTY_FILE));
    }

    public static Config loadOvercastConfigFromClasspath(String path) {
        try {
            // resource.toURI().getPath() so path gets URL decoded so spaces are no issue
            // using resource.getPath() runs into a bug with guava
            return loadOvercastConfigFromFile(Resources.getResource(path).toURI().getPath());
        } catch (IllegalArgumentException e) {
            logger.warn("File '{}' not found on classpath.", path);
        } catch (URISyntaxException e) {
            logger.warn("File '{}' not found on classpath.", path);
        }
        return ConfigFactory.empty();
    }

    public static Config loadOvercastConfigFromFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            logger.warn("File {} not found.", file.getAbsolutePath());
            return ConfigFactory.empty();
        }

        logger.info("Loading from file {}", file.getAbsolutePath());
        if (file.getName().toLowerCase().endsWith(".properties")) {
            try {
                logger.warn("The overcast.properties format is deprecated - please convert to overcast.conf format.");
                return readConfigFromProcessedFile(file);
            } catch (IOException e) {
                logger.warn("Could not read {}: {}", file.getAbsolutePath(), e.getMessage());
                logger.trace("Exception while reading file", e);
                return ConfigFactory.empty();
            }
        } else {
            return ConfigFactory.parseFile(file).resolve();
        }
    }

    public static void loadPropertiesFromClasspath(String path, final Properties properties) {
        insertConfigIntoProperties(loadOvercastConfigFromClasspath(path), properties);
    }

    public static void loadPropertiesFromPath(String path, final Properties properties) {
        insertConfigIntoProperties(loadOvercastConfigFromFile(path), properties);
    }

    private static void insertConfigIntoProperties(Config config, Properties properties) {
        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue().unwrapped().toString());
        }
    }

    private static Config readConfigFromProcessedFile(File file) throws IOException {
        String fileContent = on("\n").join(readLines(file, defaultCharset()));
        String processedFileContent = processed(fileContent, file.getName());
        ConfigParseOptions options = ConfigParseOptions.defaults().setSyntax(ConfigSyntax.PROPERTIES);
        return ConfigFactory.parseString(processedFileContent, options);
    }

    private static String processed(String s, String tplName) {
        Map<? super Object, ? super Object> model = Maps.newHashMap();
        model.put("env", System.getenv());

        try {
            StringWriter processedXml = new StringWriter();
            new Template(tplName, new StringReader(s), new Configuration()).process(model, processedXml);
            return processedXml.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}



