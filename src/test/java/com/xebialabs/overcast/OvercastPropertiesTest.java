package com.xebialabs.overcast;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.Config;

import static com.xebialabs.overcast.OvercastProperties.getOvercastProperty;
import static com.xebialabs.overcast.OvercastProperties.getRequiredOvercastProperty;
import static com.xebialabs.overcast.OvercastProperties.parsePortsProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class OvercastPropertiesTest {

    @Before
    public void setup() {
        System.setProperty("user.home", new File("src/test/resources/fake-home").getAbsolutePath());
    }

    @Test
    public void testGetOvercastProperty() throws Exception {
        assertThat(getOvercastProperty("unittestHost.vagrantDir"), is("/httpd"));
        assertThat(getOvercastProperty("unittestHost.doesNotExist", "default"), is("default"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetRequiredOvercastProperty() throws Exception {
        getRequiredOvercastProperty("unittestHost.doesNotExist");
    }

    @Test
    public void testParsePortsProperty() throws Exception {
        Map<Integer,Integer> integerIntegerMap = parsePortsProperty("2222:22,1445:445");

        assertThat(integerIntegerMap.size(), is(2));
        assertThat(integerIntegerMap.get(22), is(2222));
        assertThat(integerIntegerMap.get(445), is(1445));
    }

    @Test
    public void testHaveSystemProperties() throws IOException {
        Config cfg = PropertiesLoader.loadOvercastConfig();
        assertThat(cfg.getString("user.dir"), is(System.getProperty("user.dir")));
    }

    @Test
    public void testSubstitution() throws Exception {
        assertThat(getOvercastProperty("some.bar"), is("foo"));
        // to document that no substitution is done inside a string...
        assertThat(getOvercastProperty("some.boz"), is("${value}"));
    }

    @Test
    public void testReplaceEnvVariables() throws Exception {
        assertThat(getOvercastProperty("unittestHost.home"), is(notNullValue()));
        assertThat(getOvercastProperty("unittestHost.home").contains("${"), is(false));
    }

    @Test
    public void testSystemHasPrecedenceOverRest() {
        System.setProperty("precedenceTestValue", "valueFromEnv");
        OvercastProperties.reloadOvercastProperties();

        assertThat(OvercastProperties.getOvercastProperty("precedenceTestValue"), is("valueFromEnv"));
    }

    @Test
    public void testHomeDirHasPrecedenceOverClasspath() {
        System.clearProperty("precedenceTestValue");
        OvercastProperties.reloadOvercastProperties();

        assertThat(OvercastProperties.getOvercastProperty("precedenceTestValue"), is("valueFromHome"));
    }

    @Test
    public void testWorkDirHasPrecedenceOverClasspath() {
        System.setProperty("user.home", new File("src/test/resources/dir-without-conf").getAbsolutePath());
        System.clearProperty("precedenceTestValue");
        OvercastProperties.reloadOvercastProperties();

        assertThat(OvercastProperties.getOvercastProperty("precedenceTestValue"), is("valueFromWork"));
    }
}
