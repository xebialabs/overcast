/**
 *    Copyright 2012-2016 XebiaLabs B.V.
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigUtil;
import com.typesafe.config.ConfigValue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;

/**
 * Methods to load and access the {@code overcast.conf} file.
 */
public class OvercastProperties {
    public static final String PASSWORD_PROPERTY_SUFFIX = ".password";

    private static Logger logger = LoggerFactory.getLogger(OvercastProperties.class);

    private static Config overcastProperties;

    private static Config getOvercastConfig() {
        if (overcastProperties == null) {
            overcastProperties = PropertiesLoader.loadOvercastConfig();
        }
        return overcastProperties;
    }

    public static void reloadOvercastProperties() {
        ConfigFactory.invalidateCaches();
        overcastProperties = null;
    }

    /** Get set of property names directly below path. */
    public static Set<String> getOvercastPropertyNames(final String path) {
        Set<String> names = Sets.newHashSet();

        Config overcastConfig = getOvercastConfig();
        if (!overcastConfig.hasPath(path)) {
            return names;
        }

        Config cfg = overcastConfig.getConfig(path);

        Collection<String> tmp = Collections2.transform(cfg.entrySet(), new Function<Entry<String, ConfigValue>, String>() {
            @Override
            public String apply(Entry<String, ConfigValue> mapping) {
                String key = mapping.getKey();
                return ConfigUtil.splitPath(key).get(0);
            }
        });
        names.addAll(tmp);

        return names;
    }


    public static String getOvercastProperty(String key) {
        return getOvercastProperty(key, null);
    }

    public static String getOvercastProperty(String key, String defaultValue) {
        String value;
        Config overcastConfig = getOvercastConfig();
        if (overcastConfig.hasPath(key)) {
            value = overcastConfig.getString(key);
        } else {
            value = defaultValue;
        }
        if (logger.isTraceEnabled()) {
            if (value == null) {
                logger.trace("Overcast property {} is null", key);
            } else {
                logger.trace("Overcast property {}={}", key, key.endsWith(PASSWORD_PROPERTY_SUFFIX) ? "********" : value);
            }
        }
        return value;
    }

    public static boolean getOvercastBooleanProperty(String key) {
        return getOvercastBooleanProperty(key, false);
    }

    public static boolean getOvercastBooleanProperty(String key, boolean defaultValue) {
        boolean value;
        Config overcastConfig = getOvercastConfig();
        if (overcastConfig.hasPath(key)) {
            value = overcastConfig.getBoolean(key);
        } else {
            value = defaultValue;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Overcast property {}={}", key, key.endsWith(PASSWORD_PROPERTY_SUFFIX) ? "********" : value);
        }
        return value;
    }

    public static List<String> getOvercastListProperty(String key) {
        return getOvercastListProperty(key, Lists.<String>newArrayList());
    }

    public static List<String> getOvercastListProperty(String key, List<String> defaultValue) {
        List<String> value;
        Config overcastConfig = getOvercastConfig();
        if (overcastConfig.hasPath(key)) {
            value = overcastConfig.getStringList(key);
        } else {
            value = defaultValue;
        }
        if (logger.isTraceEnabled()) {
            if (value == null) {
                logger.trace("Overcast property {} is null", key);
            } else {
                logger.trace("Overcast property {}={}", key, key.endsWith(PASSWORD_PROPERTY_SUFFIX) ? "********" : value);
            }
        }
        return value;
    }

    public static String getRequiredOvercastProperty(String key) {
        String value = getOvercastProperty(key);
        checkState(value != null, "Required property %s is not specified as a system property or in " + PropertiesLoader.OVERCAST_CONF_FILE
                + " which can be placed in the current working directory, in ~/.overcast or on the classpath", key);
        return value;
    }

    public static Map<Integer, Integer> parsePortsProperty(String ports) {
        Map<Integer, Integer> portForwardMap = newLinkedHashMap();
        StringTokenizer toker = new StringTokenizer(ports, ",");
        while (toker.hasMoreTokens()) {
            String[] localAndRemotePort = toker.nextToken().split(":");
            checkArgument(localAndRemotePort.length == 2, "Property value \"%s\" does not have the right format, e.g. 2222:22,1445:445", ports);
            try {
                int localPort = Integer.parseInt(localAndRemotePort[0]);
                int remotePort = Integer.parseInt(localAndRemotePort[1]);
                portForwardMap.put(remotePort, localPort);
            } catch (NumberFormatException exc) {
                throw new IllegalArgumentException("Property value \"" + ports + "\" does not have the right format, e.g. 2222:22,1445:445", exc);
            }
        }
        return portForwardMap;
    }
}
