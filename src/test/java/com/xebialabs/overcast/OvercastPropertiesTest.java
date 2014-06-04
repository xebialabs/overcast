package com.xebialabs.overcast;

import java.util.Map;

import org.junit.Test;

import static com.xebialabs.overcast.OvercastProperties.getOvercastProperty;
import static com.xebialabs.overcast.OvercastProperties.getRequiredOvercastProperty;
import static com.xebialabs.overcast.OvercastProperties.parsePortsProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class OvercastPropertiesTest {

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
    public void shouldReplaceEnvVariables() throws Exception {
        assertThat(getOvercastProperty("unittestHost.home"), is(notNullValue()));
        assertThat(getOvercastProperty("unittestHost.home").contains("${"), is(false));
    }
}
