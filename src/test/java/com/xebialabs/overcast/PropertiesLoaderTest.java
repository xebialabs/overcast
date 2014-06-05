package com.xebialabs.overcast;

import java.io.IOException;

import org.junit.Test;

import com.typesafe.config.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PropertiesLoaderTest {

    @Test
    public void shouldLoadPropertiesFromPathWithSpace() throws IOException {
        Config cfg = PropertiesLoader.loadOvercastConfigFromFile("src/test/resources/with space/test.conf");
        assertThat(cfg.getString("foo"), is("bar"));
    }

    @Test
    public void shouldLoadPropertiesFromClassPathWithSpace() throws IOException {
        Config cfg = PropertiesLoader.loadOvercastConfigFromClasspath("with space/test.conf");
        assertThat(cfg.getString("foo"), is("bar"));
    }

    @Test
    public void shouldLoadPropertiesFromPath() throws IOException {
        Config cfg = PropertiesLoader.loadOvercastConfigFromFile("src/test/resources/overcast.conf");
        assertThat(cfg.getString("unittestHost.someProp"), is("someValue"));
    }

    @Test
    public void shouldLoadPropertiesFromClassPath() throws IOException {
        Config cfg = PropertiesLoader.loadOvercastConfigFromClasspath("overcast.conf");
        assertThat(cfg.getString("unittestHost.someProp"), is("someValue"));
    }

    @Test
    public void shouldLoadConfigFromClassPath() {
        Config config = PropertiesLoader.loadOvercastConfigFromClasspath("overcast.conf");
        boolean isWin = System.getProperty("os.name").contains("Windows");
        assertThat(config, notNullValue());
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
}
