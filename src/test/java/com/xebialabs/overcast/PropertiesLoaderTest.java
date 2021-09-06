/**
 *    Copyright 2012-2021 Digital.ai
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xebialabs.overcast;

import com.typesafe.config.Config;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertiesLoaderTest {

    @Test
    public void shouldLoadPropertiesFromPathWithSpace() {
        Config cfg = PropertiesLoader.loadOvercastConfigFromFile("src/test/resources/with space/test.conf").resolve();
        assertThat(cfg.getString("foo"), is("bar"));
    }

    @Test
    public void shouldLoadPropertiesFromClassPathWithSpace() {
        Config cfg = PropertiesLoader.loadOvercastConfigFromClasspath("with space/test.conf").resolve();
        assertThat(cfg.getString("foo"), is("bar"));
    }

    @Test
    public void shouldLoadPropertiesFromPath() {
        Config cfg = PropertiesLoader.loadOvercastConfigFromFile("src/test/resources/overcast.conf").resolve();
        assertThat(cfg.getString("unittestHost.someProp"), is("someValue"));
    }

    @Test
    public void shouldLoadEmptyPropertiesFromNull() {
        Config cfg = PropertiesLoader.loadOvercastConfigFromFile(null).resolve();
        assertNotNull(cfg);
        assertTrue(cfg.isEmpty());
    }

    @Test
    public void shouldLoadPropertiesFromClassPath() {
        Config cfg = PropertiesLoader.loadOvercastConfigFromClasspath("overcast.conf").resolve();
        assertThat(cfg.getString("unittestHost.someProp"), is("someValue"));
    }

    @Test
    public void shouldLoadConfigFromClassPath() {
        Config config = PropertiesLoader.loadOvercastConfigFromClasspath("overcast.conf").resolve();
        boolean isWin = System.getProperty("os.name").contains("Windows");
        assertThat(config, notNullValue());
        assertThat(config.hasPath("some.nested.namespace.stringproperty"), is(true));
        assertThat(config.getString("some.nested.namespace.stringproperty"), is("somevalue"));
        assertThat(config.hasPath("some.intprop"), is(true));
        assertThat(config.getInt("some.intprop"), is(42));
        assertThat(config.getInt("another.namespace.copiedValue"), is(42));
        if (isWin) {
            assertThat(config.getString("another.namespace.winHome"), is(System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH")));
            assertThat(config.hasPath("another.namespace.unixHome"), is(false));
        } else {
            assertThat(config.getString("another.namespace.unixHome"), is(System.getenv("HOME")));
            assertThat(config.getString("another.namespace.winHome"), is(""));
        }
    }
}
