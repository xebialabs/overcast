package com.xebialabs.overcast;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
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
    public static final String OVERCAST_USER_DIR = ".overcast";

    public static Properties loadOvercastProperties() {
        try {
            Properties properties = new Properties();
            loadPropertiesFromClasspath(OVERCAST_PROPERTY_FILE, properties);
            loadPropertiesFromPath(OVERCAST_PROPERTY_FILE, properties);
            loadPropertiesFromPath(getUserOvercastProperties(), properties);
            return properties;
        } catch (IOException exc) {
            throw new RuntimeException("Cannot load " + OVERCAST_PROPERTY_FILE, exc);
        }
    }

    private static String getUserOvercastProperties() {
        return new File(new File(System.getProperty("user.home"), OVERCAST_USER_DIR), OVERCAST_PROPERTY_FILE).getAbsolutePath();
    }

    public static void loadPropertiesFromClasspath(String path, final Properties properties) throws IOException {
        try {
            URL resource = Resources.getResource(path);
            // resource.toURI().getPath() so path gets URL decoded so spaces are no issue
            // using resource.getPath() runs into a bug with guava
            loadOvercastPropertiesFromFile(new File(resource.toURI().getPath()), properties);
        } catch (IllegalArgumentException e) {
            logger.warn("File '{}' not found on classpath.", path);
        } catch (URISyntaxException e) {
            logger.warn("File '{}' not found on classpath.", path);
        }
    }

    public static void loadPropertiesFromPath(String path, final Properties properties) throws IOException {
        loadOvercastPropertiesFromFile(new File(path), properties);
    }

    private static void loadOvercastPropertiesFromFile(File file, Properties properties) throws IOException {
        if (file.exists()) {
            logger.info("Loading from file {}", file.getAbsolutePath());
            String fileContent = on("\n").join(readLines(file, defaultCharset()));
            String processedFileContent = processed(fileContent, file.getName());

            ConfigParseOptions options = getOptions(file);
            Config config = ConfigFactory.parseString(processedFileContent, options);
            for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue().unwrapped().toString());
            }
        } else {
            logger.warn("File {} not found.", file.getAbsolutePath());
        }
    }

    private static ConfigParseOptions getOptions(File file) {
        return ConfigParseOptions.defaults().setSyntax(ConfigSyntax.PROPERTIES);
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



