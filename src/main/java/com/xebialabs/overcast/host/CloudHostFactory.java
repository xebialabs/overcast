/**
 *    Copyright 2012-2015 XebiaLabs B.V.
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
package com.xebialabs.overcast.host;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.libvirt.Connect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.xebialabs.overcast.OvercastProperties;
import com.xebialabs.overcast.command.Command;
import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.support.docker.Config;
import com.xebialabs.overcast.support.libvirt.Filesystem;
import com.xebialabs.overcast.support.libvirt.Filesystem.AccessMode;
import com.xebialabs.overcast.support.libvirt.IpLookupStrategy;
import com.xebialabs.overcast.support.libvirt.LibvirtUtil;
import com.xebialabs.overcast.support.vagrant.VagrantDriver;
import com.xebialabs.overcast.support.virtualbox.VirtualboxDriver;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.cifs.CifsConnectionBuilder;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionType;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.xebialabs.overcast.OvercastProperties.*;
import static com.xebialabs.overcast.command.CommandProcessor.atCurrentDir;
import static com.xebialabs.overcast.command.CommandProcessor.atLocation;
import static com.xebialabs.overcast.host.CachedLibvirtHost.CACHE_EXPIRATION_CMD;
import static com.xebialabs.overcast.host.CachedLibvirtHost.CACHE_EXPIRATION_URL;
import static com.xebialabs.overcast.host.CachedLibvirtHost.COPY_SPEC;
import static com.xebialabs.overcast.host.CachedLibvirtHost.PROVISIONED_BOOT_DELAY;
import static com.xebialabs.overcast.host.CachedLibvirtHost.PROVISION_CMD;
import static com.xebialabs.overcast.host.CachedLibvirtHost.PROVISION_START_TIMEOUT;
import static com.xebialabs.overcast.host.CachedLibvirtHost.PROVISION_START_TIMEOUT_DEFAULT;
import static com.xebialabs.overcast.host.CachedLibvirtHost.PROVISION_URL;
import static com.xebialabs.overcast.host.LibvirtHost.LIBVIRT_BOOT_DELAY_DEFAULT;
import static com.xebialabs.overcast.host.LibvirtHost.LIBVIRT_BOOT_DELAY_PROPERTY_SUFFIX;
import static com.xebialabs.overcast.host.LibvirtHost.LIBVIRT_FS_MAPPING_SUFFIX;
import static com.xebialabs.overcast.host.LibvirtHost.LIBVIRT_NETWORK_DEVICE_ID_PROPERTY_SUFFIX;
import static com.xebialabs.overcast.host.LibvirtHost.LIBVIRT_START_TIMEOUT_DEFAULT;
import static com.xebialabs.overcast.host.LibvirtHost.LIBVIRT_START_TIMEOUT_PROPERTY_SUFFIX;
import static com.xebialabs.overcast.host.LibvirtHost.LIBVIRT_URL_DEFAULT;
import static com.xebialabs.overcast.host.LibvirtHost.LIBVIRT_URL_PROPERTY_SUFFIX;

public class CloudHostFactory {

    public static final String HOSTNAME_PROPERTY_SUFFIX = ".hostname";

    public static final String TUNNEL_USERNAME_PROPERTY_SUFFIX = ".tunnel.username";
    public static final String TUNNEL_PASSWORD_PROPERTY_SUFFIX = ".tunnel" + OvercastProperties.PASSWORD_PROPERTY_SUFFIX;
    public static final String TUNNEL_PORTS_PROPERTY_SUFFIX = ".tunnel.ports";
    public static final String TUNNEL_SETUP_TIMEOUT = ".tunnel.setupTimeout";
    public static final String TUNNEL_DEFAULT_SETUP_TIMEOUT = "0";

    private static final String VAGRANT_DIR_PROPERTY_SUFFIX = ".vagrantDir";
    private static final String VAGRANT_VM_PROPERTY_SUFFIX = ".vagrantVm";
    private static final String VAGRANT_IP_PROPERTY_SUFFIX = ".vagrantIp";
    private static final String VAGRANT_SNAPSHOT_EXPIRATION_CMD = ".vagrantSnapshotExpirationCmd";
    private static final String VAGRANT_OS_PROPERTY_SUFFIX = ".vagrantOs";

    private static final String VBOX_UUID_PROPERTY_SUFFIX = ".vboxUuid";
    private static final String VBOX_IP = ".vboxBoxIp";
    private static final String VBOX_SNAPSHOT = ".vboxSnapshotUuid";

    public static Logger logger = LoggerFactory.getLogger(CloudHostFactory.class);

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
        String hostName = getOvercastProperty(label + HOSTNAME_PROPERTY_SUFFIX);
        if (hostName != null) {
            return createExistingCloudHost(label);
        }

        String vagrantDir = getOvercastProperty(label + VAGRANT_DIR_PROPERTY_SUFFIX);
        if (vagrantDir != null) {
            return createVagrantCloudHost(label, vagrantDir);
        }

        String amiId = getOvercastProperty(label + Ec2CloudHost.AMI_ID_PROPERTY_SUFFIX);
        if (amiId != null) {
            return createEc2CloudHost(label, amiId, disableEc2);
        }

        String vboxUuid = getOvercastProperty(label + VBOX_UUID_PROPERTY_SUFFIX);
        if (vboxUuid != null) {
            return createVboxHost(label, vboxUuid);
        }

        String kvmBaseDomain = getOvercastProperty(label + LibvirtHost.LIBVIRT_BASE_DOMAIN_PROPERTY_SUFFIX);
        if (kvmBaseDomain != null) {
            return createLibvirtHost(label, kvmBaseDomain);
        }

        String dockerImage = getOvercastProperty(label + Config.DOCKER_IMAGE_SUFFIX);
        if (dockerImage != null) {
            return createDockerHost(label, dockerImage);
        }

        throw new IllegalStateException("No valid configuration has been specified for host label " + label);
    }

    private static CloudHost createDockerHost(String label, String imageName) {

        String image = getOvercastProperty(label + Config.DOCKER_IMAGE_SUFFIX, Config.DOCKER_DEFAULT_IMAGE);
        String dockerHostName = getOvercastProperty(label + Config.DOCKER_HOST_SUFFIX, Config.DOCKER_DEFAULT_HOST);
        String certicates = getOvercastProperty(label + Config.DOCKER_CERTIFICATES, null);
        DockerHost dockerHost = new DockerHost(image, dockerHostName, Strings.isNullOrEmpty(certicates) ? null : new File(certicates).toPath());

        dockerHost.setName(getOvercastProperty(label + Config.DOCKER_NAME_SUFFIX));
        dockerHost.setCommand(getOvercastListProperty(label + Config.DOCKER_COMMAND_SUFFIX));
        dockerHost.setExposeAllPorts(getOvercastBooleanProperty(label + Config.DOCKER_EXPOSE_ALL_PORTS_SUFFIX));
        dockerHost.setRemove(getOvercastBooleanProperty(label + Config.DOCKER_REMOVE_SUFFIX));
        dockerHost.setRemoveVolume(getOvercastBooleanProperty(label + Config.DOCKER_REMOVE_VOLUME_SUFFIX));
        dockerHost.setEnv(getOvercastListProperty(label + Config.DOCKER_ENV_SUFFIX));
        dockerHost.setExposedPorts(newHashSet(getOvercastListProperty(label + Config.DOCKER_EXPOSED_PORTS_SUFFIX)));

        return dockerHost;
    }

    private static CloudHost createLibvirtHost(String label, String kvmBaseDomain) {
        String libvirtURL = getOvercastProperty(label + LIBVIRT_URL_PROPERTY_SUFFIX, LIBVIRT_URL_DEFAULT);

        Connect libvirt = LibvirtUtil.getConnection(libvirtURL, false);

        int startTimeout = Integer.valueOf(getOvercastProperty(label + LIBVIRT_START_TIMEOUT_PROPERTY_SUFFIX, LIBVIRT_START_TIMEOUT_DEFAULT));
        int bootDelay = Integer.valueOf(getOvercastProperty(label + LIBVIRT_BOOT_DELAY_PROPERTY_SUFFIX, LIBVIRT_BOOT_DELAY_DEFAULT));
        String networkName = getOvercastProperty(label + LIBVIRT_NETWORK_DEVICE_ID_PROPERTY_SUFFIX);

        List<Filesystem> fsMappings = newArrayList();
        Set<String> mappingNames = getOvercastPropertyNames(label + LIBVIRT_FS_MAPPING_SUFFIX);
        for (String mapping : mappingNames) {
            fsMappings.add(createFilesystem(mapping, label + LIBVIRT_FS_MAPPING_SUFFIX + "." + mapping));
        }

        IpLookupStrategy ipLookupStrategy = LibvirtHost.determineIpLookupStrategy(label);

        String provisionCmd = getOvercastProperty(label + PROVISION_CMD);

        if (provisionCmd == null) {
            return new LibvirtHost(libvirt, kvmBaseDomain, ipLookupStrategy, networkName, startTimeout, bootDelay, fsMappings);
        } else {
            String provisionUrl = getRequiredOvercastProperty(label + PROVISION_URL);
            String cacheExpirationUrl = getOvercastProperty(label + CACHE_EXPIRATION_URL);
            String cacheExpirationCmd = getRequiredOvercastProperty(label + CACHE_EXPIRATION_CMD);
            int provisionStartTimeout = Integer.valueOf(getOvercastProperty(label + PROVISION_START_TIMEOUT, PROVISION_START_TIMEOUT_DEFAULT));
            int provisionedBootDelay = Integer.valueOf(getOvercastProperty(label + PROVISIONED_BOOT_DELAY, LIBVIRT_BOOT_DELAY_DEFAULT));
            List<String> copySpec = getOvercastListProperty(label + COPY_SPEC, Collections.<String> emptyList());
            CommandProcessor cmdProcessor = atCurrentDir();

            return new CachedLibvirtHost(label, libvirt, kvmBaseDomain, ipLookupStrategy, networkName, provisionUrl, provisionCmd, cacheExpirationUrl,
                cacheExpirationCmd, cmdProcessor, startTimeout, bootDelay, provisionStartTimeout, provisionedBootDelay, fsMappings, copySpec);
        }
    }

    private static Filesystem createFilesystem(String target, String path) {
        String source = getRequiredOvercastProperty(path + ".hostPath");
        AccessMode accessMode = AccessMode.valueOf(getOvercastProperty(path + ".accessMode", AccessMode.PASSTHROUGH.toString()));
        boolean readOnly = Boolean.valueOf(getOvercastProperty(path + ".readOnly", "true"));
        return new Filesystem(source, target, accessMode, readOnly);
    }

    private static CloudHost createVboxHost(final String label, final String vboxUuid) {
        String vboxIp = getOvercastProperty(label + VBOX_IP);
        String vboxSnapshot = getOvercastProperty(label + VBOX_SNAPSHOT);
        return new VirtualboxHost(vboxIp, vboxUuid, vboxSnapshot);
    }

    private static CloudHost createExistingCloudHost(final String label) {
        logger.info("Using existing host for {}", label);
        return new ExistingCloudHost(label);
    }

    private static CloudHost createVagrantCloudHost(final String hostLabel, final String vagrantDir) {
        String vagrantVm = getOvercastProperty(hostLabel + VAGRANT_VM_PROPERTY_SUFFIX);
        String vagrantIp = getOvercastProperty(hostLabel + VAGRANT_IP_PROPERTY_SUFFIX);
        String vagrantExpirationCmd = getOvercastProperty(hostLabel + VAGRANT_SNAPSHOT_EXPIRATION_CMD);
        String vagrantOs = getOvercastProperty(hostLabel + VAGRANT_OS_PROPERTY_SUFFIX, OperatingSystemFamily.UNIX.toString());

        logger.info("Using Vagrant to create {}", hostLabel);

        CommandProcessor cmdProcessor = atLocation(vagrantDir);
        VagrantDriver vagrantDriver = new VagrantDriver(hostLabel, cmdProcessor);
        VirtualboxDriver vboxDriver = new VirtualboxDriver(cmdProcessor);

        if (vagrantExpirationCmd == null) {
            return new VagrantCloudHost(vagrantVm, vagrantIp, vagrantDriver);
        } else {
            ConnectionOptions options = new ConnectionOptions();
            options.set(ConnectionOptions.ADDRESS, vagrantIp);
            options.set(ConnectionOptions.USERNAME, "vagrant");
            options.set(ConnectionOptions.PASSWORD, "vagrant");

            OverthereConnectionBuilder cb = null;
            if (OperatingSystemFamily.WINDOWS.toString().equals(vagrantOs)) {
                options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.WINDOWS);
                options.set(SshConnectionBuilder.CONNECTION_TYPE, CifsConnectionType.WINRM_INTERNAL);

                cb = new CifsConnectionBuilder("winrm", options, new DefaultAddressPortMapper());
            } else {
                options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
                options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SFTP);

                cb = new SshConnectionBuilder("ssh", options, new DefaultAddressPortMapper());
            }
            return new CachedVagrantCloudHost(vagrantVm, vagrantIp, Command.fromString(vagrantExpirationCmd), vagrantDriver, vboxDriver, cmdProcessor, cb);
        }
    }

    private static CloudHost createEc2CloudHost(final String label, final String amiId, final boolean disableEc2) {
        if (disableEc2) {
            throw new IllegalStateException("Only an AMI ID (" + amiId + ") has been specified for host label " + label
                + ", but EC2 hosts are not available.");
        }
        logger.info("Using Amazon EC2 for {}", label);
        return new Ec2CloudHost(label, amiId);
    }

    private static CloudHost wrapCloudHost(String label, CloudHost actualHost) {
        String tunnelUsername = getOvercastProperty(label + TUNNEL_USERNAME_PROPERTY_SUFFIX);
        if (tunnelUsername == null) {
            return actualHost;
        }

        logger.info("Starting SSH tunnels for {}", label);

        String tunnelPassword = getRequiredOvercastProperty(label + TUNNEL_PASSWORD_PROPERTY_SUFFIX);
        String ports = getRequiredOvercastProperty(label + TUNNEL_PORTS_PROPERTY_SUFFIX);
        int timeout = Integer.parseInt(getOvercastProperty(label + TUNNEL_SETUP_TIMEOUT, TUNNEL_DEFAULT_SETUP_TIMEOUT));
        Map<Integer, Integer> portForwardMap = parsePortsProperty(ports);
        return new TunneledCloudHost(actualHost, tunnelUsername, tunnelPassword, portForwardMap, timeout);
    }
}
