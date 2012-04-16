/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.cloudhost;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class CloudHostFactory {

	private static final String CLOUD_HOST_PROPERTY_FILE = "cloudhost.properties";

	public static final String HOSTNAME_PROPERTY_SUFFIX = ".hostname";

	public static final String AMI_ID_PROPERTY_SUFFIX = ".amiId";
	public static final String AWS_ENDPOINT_PROPERTY = "aws.endpoint";
	public static final String AWS_ENDPOINT_DEFAULT = "https://ec2.amazonaws.com";
	public static final String AWS_ACCESS_KEY_PROPERTY = "aws.accessKey";
	public static final String AWS_SECRET_KEY_PROPERTY = "aws.secretKey";
	public static final String AMI_AVAILABILITY_ZONE_PROPERTY_SUFFIX = ".amiAvailabilityZone";
	public static final String AMI_INSTANCE_TYPE_PROPERTY_SUFFIX = ".amiInstanceType";
	public static final String AMI_SECURITY_GROUP_PROPERTY_SUFFIX = ".amiSecurityGroup";
	public static final String AMI_KEY_NAME_PROPERTY_SUFFIX = ".amiKeyName";
	public static final String AMI_BOOT_SECONDS_PROPERTY_SUFFIX = ".amiBootSeconds";

	public static final String TUNNEL_USERNAME_PROPERTY_SUFFIX = ".tunnel.username";
	public static final String TUNNEL_PASSWORD_PROPERTY_SUFFIX = ".tunnel.password";
	public static final String TUNNEL_PORTS_PROPERTY_SUFFIX = ".tunnel.ports";

	// The field logger needs to be defined up here so that the static
	// initialized below can use the logger
	public static Logger logger = LoggerFactory.getLogger(CloudHostFactory.class);

	private static Properties cloudHostProperties;

	static {
		loadCloudHostProperties();
	}

	public static CloudHost getCloudHostWithNoTeardown(String hostLabel) {
		return getCloudHost(hostLabel, true);
	}

	public static CloudHost getCloudHost(String hostLabel) {
		return getCloudHost(hostLabel, false);
	}

	private static CloudHost getCloudHost(String hostLabel, boolean disableEc2) {
		CloudHost host = createCloudHost(hostLabel, disableEc2);
		return wrapCloudHost(hostLabel, host);
	}

	protected static CloudHost createCloudHost(String label, boolean disableEc2) {
		String hostName = getCloudHostProperty(label + HOSTNAME_PROPERTY_SUFFIX);
		if (hostName != null) {
			logger.info("Using existing host for {}", label);
			return new ExistingCloudHost(label);
		}

		String amiId = getCloudHostProperty(label + AMI_ID_PROPERTY_SUFFIX);
		if (amiId != null) {
			if (disableEc2) {
				throw new IllegalStateException("Only an AMI ID (" + amiId + ") has been specified for host label " + label
						+ ", but EC2 hosts are not available.");
			}
			logger.info("Using Amazon EC2 for {}", label);
			return new Ec2CloudHost(label, amiId);
		}

		throw new IllegalStateException("Neither a hostname (" + hostName + ") nor an AMI id (" + amiId + ") have been specified for host label " + label);
	}

	private static CloudHost wrapCloudHost(String label, CloudHost actualHost) {
		String tunnelUsername = getCloudHostProperty(label + TUNNEL_USERNAME_PROPERTY_SUFFIX);
		if (tunnelUsername == null) {
			return actualHost;
		}

		logger.info("Starting SSH tunnels for {}", label);

		String tunnelPassword = getRequiredCloudHostProperty(label + TUNNEL_PASSWORD_PROPERTY_SUFFIX);
		String ports = getRequiredCloudHostProperty(label + TUNNEL_PORTS_PROPERTY_SUFFIX);
		Map<Integer, Integer> portForwardMap = parsePortsProperty(ports);
		return new TunneledCloudHost(actualHost, tunnelUsername, tunnelPassword, portForwardMap);
	}

	private static Map<Integer, Integer> parsePortsProperty(String ports) {
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

	private static void loadCloudHostProperties() {
		try {
			cloudHostProperties = new Properties();
			loadCloudHostPropertiesFromClasspath();
			loadCloudHostPropertiesFromHomeDirectory();
			loadCloudHostPropertiesFromCurrentDirectory();
		} catch (IOException exc) {
			throw new RuntimeException("Cannot load " + CLOUD_HOST_PROPERTY_FILE, exc);
		}
	}

	private static void loadCloudHostPropertiesFromClasspath() throws IOException {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(CLOUD_HOST_PROPERTY_FILE);
		if (resource != null) {
			InputStream in = resource.openStream();
			try {
				logger.info("Loading {}", resource);
				cloudHostProperties.load(in);
			} finally {
				in.close();
			}
		} else {
			logger.warn("File {} not found on classpath.", CLOUD_HOST_PROPERTY_FILE);
		}
	}

	private static void loadCloudHostPropertiesFromHomeDirectory() throws FileNotFoundException, IOException {
		loadCloudHostPropertiesFromFile(new File(System.getProperty("user.home"), ".cloudhost/" + CLOUD_HOST_PROPERTY_FILE));
	}

	private static void loadCloudHostPropertiesFromCurrentDirectory() throws FileNotFoundException, IOException {
		loadCloudHostPropertiesFromFile(new File(CLOUD_HOST_PROPERTY_FILE));
	}

	private static void loadCloudHostPropertiesFromFile(File file) throws FileNotFoundException, IOException {
		if (file.exists()) {
			FileInputStream in = new FileInputStream(file);
			try {
				logger.info("Loading {}", file.getAbsolutePath());
				cloudHostProperties.load(in);
			} finally {
				in.close();
			}
		} else {
			logger.warn("File {} not found.", file.getAbsolutePath());
		}
	}

	public static String getRequiredCloudHostProperty(String key) {
		String value = getCloudHostProperty(key);
		checkState(
				value != null,
				"Required property %s is not specified as a system property or in " + CLOUD_HOST_PROPERTY_FILE
						+ " which can be placed in the current working directory, in ~/.cloudhost or on the classpath",
				key);
		return value;
	}

	public static String getCloudHostProperty(String key) {
		return getCloudHostProperty(key, null);
	}

	public static String getCloudHostProperty(String key, String defaultValue) {
		String value = System.getProperty(key);
		if (value == null) {
			value = cloudHostProperties.getProperty(key, defaultValue);
		}
		if (logger.isTraceEnabled()) {
			if (value == null) {
				logger.trace("CloudHost property {} is null", key);
			} else {
				logger.trace("CloudHost property {}={}", key, key.endsWith(TUNNEL_PASSWORD_PROPERTY_SUFFIX) ? "********" : value);
			}
		}
		return value;
	}

}
