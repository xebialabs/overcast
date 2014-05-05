package com.xebialabs.overcast;

import com.typesafe.config.Config;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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

    @Test
    public void shouldLoadConfigFromFile() {
        Config config = PropertiesLoader.loadOvercastConfigFromClasspath("overcast.conf");
        boolean isWin = System.getProperty("os.name").contains("Windows");
        assertThat(config, notNullValue());
        assertThat(config.entrySet().size(), is(isWin ? 4 : 5));
        assertThat(config.hasPath("some.nested.namespace.stringproperty"), is(true));
        assertThat(config.getString("some.nested.namespace.stringproperty"), is("somevalue"));
        assertThat(config.hasPath("some.intprop"), is(true));
        assertThat(config.getInt("some.intprop"), is(42));
        assertThat(config.getInt("another.namespace.copiedValue"), is(42));
        if (isWin) {
            assertThat(config.getString("another.namespace.winHome"), is(System.getenv("HOMEDRIVE")+System.getenv("HOMEPATH")));
            assertThat(config.hasPath("another.namespace.unixHome"), is(false));
        } else {
            assertThat(config.getString("another.namespace.unixHome"), is(System.getenv("HOME")));
            assertThat(config.getString("another.namespace.winHome"), is(""));
        }
    }

    @Test
    public void shouldFallback() {
        Config config = PropertiesLoader.loadOvercastConfig();
        assertThat(config.hasPath("some.intprop"), is(true)); // from overcast.conf
        assertThat(config.hasPath("unittestHost.someProp"), is(true)); // from overcast.properties
    }

    @Test
    public void shouldLoadPropertiesFromConf() {
        Properties p = PropertiesLoader.loadOvercastProperties();
        assertThat(p.getProperty("some.intprop"), is("42"));
        assertThat(p.getProperty("unittestHost.someProp"), is("someValue"));
    }
}
