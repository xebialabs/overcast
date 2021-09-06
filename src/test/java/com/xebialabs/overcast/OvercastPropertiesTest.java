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
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.xebialabs.overcast.OvercastProperties.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OvercastPropertiesTest {

    @BeforeAll
    static void setup() {
        System.setProperty("user.home", new File("src/test/resources/fake-home").getAbsolutePath());
    }

    @AfterAll
    static void teardown() {
        System.clearProperty("overcast.conf.file");
    }

    @Test
    public void testGetOvercastProperty() {
        assertThat(getOvercastProperty("unittestHost.vagrantDir"), is("/httpd"));
        assertThat(getOvercastProperty("unittestHost.doesNotExist", "default"), is("default"));
    }

    @Test
    public void testGetRequiredOvercastProperty() {
        assertThrows(IllegalStateException.class, () -> {
            getRequiredOvercastProperty("unittestHost.doesNotExist");
        });
    }

    @Test
    public void testParsePortsProperty() {
        Map<Integer, Integer> integerIntegerMap = parsePortsProperty("2222:22,1445:445");

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
    public void testPropertyHasPrecedenceOverClasspath() throws IOException {

        System.setProperty("user.home", new File("src/test/resources/dir-without-conf").getAbsolutePath());
        System.clearProperty("precedenceTestValue");
        System.setProperty("overcast.conf.file", "src/test/resources/property-path/overcast.conf");

        File cfg = new File("overcast.conf");
        File bkp = new File("backup.conf");

        try {
            FileUtils.moveFile(cfg, bkp);
            OvercastProperties.reloadOvercastProperties();
            assertThat(OvercastProperties.getOvercastProperty("precedenceTestValue"), is("valueFromProperty"));
        } finally {
            FileUtils.moveFile(bkp, cfg);
        }
    }

    @Test
    public void testWorkDirHasPrecedenceOverProperty() {

        System.setProperty("user.home", new File("src/test/resources/dir-without-conf").getAbsolutePath());
        System.clearProperty("precedenceTestValue");
        System.setProperty("overcast.conf.file", "src/test/resources/property-path/overcast.conf");

        OvercastProperties.reloadOvercastProperties();

        assertThat(OvercastProperties.getOvercastProperty("precedenceTestValue"), is("valueFromWork"));
    }

    @Test
    public void testWorkDirHasPrecedenceOverClasspath() {
        System.setProperty("user.home", new File("src/test/resources/dir-without-conf").getAbsolutePath());
        System.clearProperty("precedenceTestValue");
        OvercastProperties.reloadOvercastProperties();

        assertThat(OvercastProperties.getOvercastProperty("precedenceTestValue"), is("valueFromWork"));
    }
}
