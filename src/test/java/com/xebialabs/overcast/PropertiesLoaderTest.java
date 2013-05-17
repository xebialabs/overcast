package com.xebialabs.overcast;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PropertiesLoaderTest {

    @Test
    public void shouldLoadPropertiesFromPathWithSpace() throws IOException {
        Properties props = new Properties();
        PropertiesLoader.loadPropertiesFromPath("src/test/resources/with space/test.properties", props);
        assertThat(props.getProperty("foo"), is("bar"));
    }

    @Test
    public void shouldLoadPropertiesFromPath() throws IOException {
        Properties props = new Properties();
        PropertiesLoader.loadPropertiesFromPath("src/test/resources/overcast.properties", props);
        assertThat(props.getProperty("unittestHost.someProp"), is("someValue"));
    }

    @Test
    public void shouldLoadPropertiesFromClassPathWithSpace() throws IOException {
        Properties props = new Properties();
        PropertiesLoader.loadPropertiesFromClasspath("with space/test.properties", props);
        assertThat(props.getProperty("foo"), is("bar"));
    }

    @Test
    public void shouldLoadPropertiesFromClassPath() throws IOException {
        Properties props = new Properties();
        PropertiesLoader.loadPropertiesFromClasspath("overcast.properties", props);
        assertThat(props.getProperty("unittestHost.someProp"), is("someValue"));
    }
}
