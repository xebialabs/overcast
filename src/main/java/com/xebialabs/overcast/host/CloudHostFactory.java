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
package com.xebialabs.overcast.host;

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
import org.libvirt.Connect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.*;

import static com.xebialabs.overcast.OvercastProperties.*;
import static com.xebialabs.overcast.Strings.isNullOrEmpty;
import static com.xebialabs.overcast.command.CommandProcessor.atCurrentDir;
import static com.xebialabs.overcast.command.CommandProcessor.atLocation;
import static com.xebialabs.overcast.host.CachedLibvirtHost.*;
import static com.xebialabs.overcast.host.VMWareHost.*;

public class CloudHostFactory {

    public static final String HOSTNAME_PROPERTY_SUFFIX = ".hostname";

    public static final String TUNNEL_USERNAME_PROPERTY_SUFFIX = ".tunnel.username";
    public static final String TUNNEL_PASSWORD_PROPERTY_SUFFIX = ".tunnel" + OvercastProperties.PASSWORD_PROPERTY_SUFFIX;
    public static final String TUNNEL_PORTS_PROPERTY_SUFFIX = ".tunnel.ports";
    public static final String TUNNEL_SETUP_TIMEOUT = ".tunnel.setupTimeout";
    public static final String TUNNEL_DEFAULT_SETUP_TIMEOUT = "0";

    private static final String VMWARE_AUTH_HASHED_CREDENTIALS = ".vmwareAuthHashCredentials";

    private static final String VAGRANT_DIR_PROPERTY_SUFFIX = ".vagrantDir";
    private static final String VAGRANT_VM_PROPERTY_SUFFIX = ".vagrantVm";
    private static final String VAGRANT_IP_PROPERTY_SUFFIX = ".vagrantIp";
    private static final String VAGRANT_PARAMETERS_SUFFIX = ".vagrantParameters";
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

        String vmwareBaseDomain = getOvercastProperty(label + VMWareHost.VMWARE_VM_BASE_IMAGE_PROPERTY_SUFFIX);
        if (vmwareBaseDomain != null) {
            return createVmWareCloudHost(label, vmwareBaseDomain);
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

    private static CloudHost createDockerHost(String label, String image) {
        String dockerHostName = getOvercastProperty(label + Config.DOCKER_HOST_SUFFIX);
        String certificates = getOvercastProperty(label + Config.DOCKER_CERTIFICATES_SUFFIX);

        DockerHost dockerHost = new DockerHost(image, dockerHostName, isNullOrEmpty(certificates) ? null : Paths.get(certificates));

        dockerHost.setName(getOvercastProperty(label + Config.DOCKER_NAME_SUFFIX));
        dockerHost.setCommand(getOvercastListProperty(label + Config.DOCKER_COMMAND_SUFFIX));
        dockerHost.setExposeAllPorts(getOvercastBooleanProperty(label + Config.DOCKER_EXPOSE_ALL_PORTS_SUFFIX));
        dockerHost.setTty(getOvercastBooleanProperty(label + Config.DOCKER_TTY_SUFFIX));
        dockerHost.setRemove(getOvercastBooleanProperty(label + Config.DOCKER_REMOVE_SUFFIX));
        dockerHost.setRemoveVolume(getOvercastBooleanProperty(label + Config.DOCKER_REMOVE_VOLUME_SUFFIX));
        dockerHost.setEnv(getOvercastListProperty(label + Config.DOCKER_ENV_SUFFIX));
        dockerHost.setExposedPorts(new HashSet<>(getOvercastListProperty(label + Config.DOCKER_EXPOSED_PORTS_SUFFIX)));
        dockerHost.setLinks(getOvercastListProperty(label + Config.DOCKER_LINKS_SUFFIX));
        dockerHost.setPortBindings(new HashSet<>(getOvercastListProperty(label + Config.DOCKER_PORT_BINDINGS_SUFFIX)));

        return dockerHost;
    }

    private static CloudHost createLibvirtHost(String label, String kvmBaseDomain) {
        String libvirtURL = getOvercastProperty(label + LIBVIRT_URL_PROPERTY_SUFFIX, LIBVIRT_URL_DEFAULT);

        Connect libvirt = LibvirtUtil.getConnection(libvirtURL, false);

        int startTimeout = Integer.parseInt(getOvercastProperty(label + LIBVIRT_START_TIMEOUT_PROPERTY_SUFFIX, LIBVIRT_START_TIMEOUT_DEFAULT));
        int bootDelay = Integer.parseInt(getOvercastProperty(label + LIBVIRT_BOOT_DELAY_PROPERTY_SUFFIX, LIBVIRT_BOOT_DELAY_DEFAULT));
        String networkName = getOvercastProperty(label + LIBVIRT_NETWORK_DEVICE_ID_PROPERTY_SUFFIX);

        List<Filesystem> fsMappings = new ArrayList<>();
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
            int provisionStartTimeout = Integer.parseInt(getOvercastProperty(label + PROVISION_START_TIMEOUT, PROVISION_START_TIMEOUT_DEFAULT));
            int provisionedBootDelay = Integer.parseInt(getOvercastProperty(label + PROVISIONED_BOOT_DELAY, LIBVIRT_BOOT_DELAY_DEFAULT));
            List<String> copySpec = getOvercastListProperty(label + COPY_SPEC, Collections.emptyList());
            CommandProcessor cmdProcessor = atCurrentDir();

            return new CachedLibvirtHost(label, libvirt, kvmBaseDomain, ipLookupStrategy, networkName, provisionUrl, provisionCmd, cacheExpirationUrl,
                    cacheExpirationCmd, cmdProcessor, startTimeout, bootDelay, provisionStartTimeout, provisionedBootDelay, fsMappings, copySpec);
        }
    }

    private static Filesystem createFilesystem(String target, String path) {
        String source = getRequiredOvercastProperty(path + ".hostPath");
        AccessMode accessMode = AccessMode.valueOf(getOvercastProperty(path + ".accessMode", AccessMode.PASSTHROUGH.toString()));
        boolean readOnly = Boolean.parseBoolean(getOvercastProperty(path + ".readOnly", "true"));
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

    private static CloudHost createVmWareCloudHost(final String hostLabel, final String vmBaseImage) {
        int connectionTimeout = Integer.parseInt(
                getOvercastProperty(hostLabel + VMWARE_TIMEOUT_PROPERTY_SUFFIX, VMWARE_TIMEOUT_DEFAULT));
        String authHash = getOvercastProperty(hostLabel + VMWARE_AUTH_HASHED_CREDENTIALS);

        boolean ignoreBadCertificate = Boolean.parseBoolean(getOvercastProperty(hostLabel + VMWARE_IGNORE_BAD_CERTIFICATE_SUFFIX,
                VMWARE_IGNORE_BAD_CERTIFICATE_DEFAULT));

        String securityAlgorithm = getOvercastProperty(hostLabel + VMWARE_SECURITY_ALGORITHM_SUFFIX,
                VMWARE_SECURITY_ALGORITHM_DEFAULT);

        String vmwareApiHost = getOvercastProperty(hostLabel + VMWARE_API_HOST_SUFFIX);

        boolean instanceClone = Boolean.parseBoolean(getOvercastProperty(hostLabel + VMWARE_INSTANCE_CLONE_SUFFIX,
                VMWARE_INSTANCE_CLONE_DEFAULT));

        int maxRetries = Integer.parseInt(
                getOvercastProperty(hostLabel + VMWARE_MAX_RETRIES_SUFFIX, VMWARE_MAX_RETRIES_DEFAULT));

        return new VMWareHost(vmwareApiHost,
                authHash,
                vmBaseImage,
                ignoreBadCertificate,
                instanceClone,
                securityAlgorithm,
                connectionTimeout,
                maxRetries);
    }

    private static CloudHost createVagrantCloudHost(final String hostLabel, final String vagrantDir) {
        String vagrantVm = getOvercastProperty(hostLabel + VAGRANT_VM_PROPERTY_SUFFIX);
        String vagrantIp = getOvercastProperty(hostLabel + VAGRANT_IP_PROPERTY_SUFFIX);
        Map<String, String> vagrantParameters = getOvercastMapProperty(hostLabel + VAGRANT_PARAMETERS_SUFFIX);
        String vagrantExpirationCmd = getOvercastProperty(hostLabel + VAGRANT_SNAPSHOT_EXPIRATION_CMD);
        String vagrantOs = getOvercastProperty(hostLabel + VAGRANT_OS_PROPERTY_SUFFIX, OperatingSystemFamily.UNIX.toString());

        logger.info("Using Vagrant to create {}", hostLabel);

        CommandProcessor cmdProcessor = atLocation(vagrantDir);
        VagrantDriver vagrantDriver = new VagrantDriver(hostLabel, cmdProcessor);
        VirtualboxDriver vboxDriver = new VirtualboxDriver(cmdProcessor);

        if (vagrantExpirationCmd == null) {
            return new VagrantCloudHost(vagrantVm, vagrantIp, vagrantDriver, vagrantParameters);
        } else {
            ConnectionOptions options = new ConnectionOptions();
            options.set(ConnectionOptions.ADDRESS, vagrantIp);
            options.set(ConnectionOptions.USERNAME, "vagrant");
            options.set(ConnectionOptions.PASSWORD, "vagrant");

            OverthereConnectionBuilder cb;
            if (OperatingSystemFamily.WINDOWS.toString().equals(vagrantOs)) {
                options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.WINDOWS);
                options.set(SshConnectionBuilder.CONNECTION_TYPE, CifsConnectionType.WINRM_INTERNAL);

                cb = new CifsConnectionBuilder("winrm", options, new DefaultAddressPortMapper());
            } else {
                options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
                options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SFTP);

                cb = new SshConnectionBuilder("ssh", options, new DefaultAddressPortMapper());
            }
            return new CachedVagrantCloudHost(vagrantVm, vagrantIp, Command.fromString(vagrantExpirationCmd),
                    vagrantDriver, vboxDriver, cmdProcessor, cb, vagrantParameters);
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
