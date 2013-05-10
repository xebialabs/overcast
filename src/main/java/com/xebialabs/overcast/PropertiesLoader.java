package com.xebialabs.overcast;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import static com.google.common.base.Joiner.on;
import static com.google.common.io.Files.readLines;
import static java.nio.charset.Charset.defaultCharset;

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
        URL resource = Resources.getResource(OVERCAST_PROPERTY_FILE);
        if (resource != null) {
            loadOvercastPropertiesFromFile(new File(resource.getFile()), properties);
        } else {
            logger.warn("File {} not found on classpath.", OVERCAST_PROPERTY_FILE);
        }
    }

    private static void loadOvercastPropertiesFromCurrentDirectory(final Properties properties) throws IOException {
        loadOvercastPropertiesFromFile(new File(OVERCAST_PROPERTY_FILE), properties);
    }

    private static void loadOvercastPropertiesFromHomeDirectory(final Properties properties) throws IOException {
        loadOvercastPropertiesFromFile(new File(System.getProperty("user.home"), ".overcast" + File.separator + OVERCAST_PROPERTY_FILE), properties);
    }

    private static void loadOvercastPropertiesFromFile(File file, Properties properties) throws IOException {
        if (file.exists()) {
            logger.info("Loading from file {}", file.getAbsolutePath());
            String fileContent = on("\n").join(readLines(file, defaultCharset()));
            properties.load(new ByteArrayInputStream(processed(fileContent, file.getName()).getBytes()));
        } else {
            logger.warn("File {} not found.", file.getAbsolutePath());
        }
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



