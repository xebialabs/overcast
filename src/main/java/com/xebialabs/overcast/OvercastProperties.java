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
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigUtil;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.xebialabs.overcast.Preconditions.checkArgument;
import static com.xebialabs.overcast.Preconditions.checkState;

/**
 * Methods to load and access the {@code overcast.conf} file.
 */
public class OvercastProperties {
    public static final String PASSWORD_PROPERTY_SUFFIX = ".password";

    private static final Logger logger = LoggerFactory.getLogger(OvercastProperties.class);

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
        Config overcastConfig = getOvercastConfig();
        if (!overcastConfig.hasPath(path)) {
            return new HashSet<>();
        }

        Config cfg = overcastConfig.getConfig(path);

        Set<String> result = new HashSet<>();
        for (Map.Entry<String, ConfigValue> e : cfg.entrySet()) {
            result.add(ConfigUtil.splitPath(e.getKey()).get(0));
        }
        return result;
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
        return getOvercastListProperty(key, new ArrayList<String>());
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

    public static Map<String, String> getOvercastMapProperty(String key) {
        return getOvercastMapProperty(key, new LinkedHashMap<>());
    }

    public static Map<String, String> getOvercastMapProperty(String key, Map<String, String> defaultValue) {
        Map<String, String> value;
        Config overcastConfig = getOvercastConfig();
        if (overcastConfig.hasPath(key)) {
            value = new LinkedHashMap<>();
            for( Map.Entry<String, ConfigValue> element : overcastConfig.getConfig(key).entrySet()) {
                value.put(element.getKey(), element.getValue().unwrapped().toString());
            }
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
        Map<Integer, Integer> portForwardMap = new LinkedHashMap<>();
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
