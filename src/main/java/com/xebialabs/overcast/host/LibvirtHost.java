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
package com.xebialabs.overcast.host;

import java.util.List;
import java.util.UUID;

import org.libvirt.Connect;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overcast.support.libvirt.DomainWrapper;
import com.xebialabs.overcast.support.libvirt.Filesystem;
import com.xebialabs.overcast.support.libvirt.IpLookupStrategy;
import com.xebialabs.overcast.support.libvirt.SshIpLookupStrategy;
import com.xebialabs.overcast.support.libvirt.StaticIpLookupStrategy;

import static com.xebialabs.overcast.OvercastProperties.getRequiredOvercastProperty;

class LibvirtHost implements CloudHost {
    private static final Logger logger = LoggerFactory.getLogger(LibvirtHost.class);

    public static final String LIBVIRT_URL_PROPERTY_SUFFIX = ".libvirtURL";

    public static final String LIBVIRT_START_TIMEOUT_PROPERTY_SUFFIX = ".libvirtStartTimeout";
    public static final String LIBVIRT_START_TIMEOUT_DEFAULT = "30";

    public static final String LIBVIRT_BOOT_DELAY_PROPERTY_SUFFIX = ".bootDelay";
    public static final String LIBVIRT_BOOT_DELAY_DEFAULT = "0";

    public static final String LIBVIRT_BASE_DOMAIN_PROPERTY_SUFFIX = ".baseDomain";
    public static final String LIBVIRT_NETWORK_DEVICE_ID_PROPERTY_SUFFIX = ".network";
    public static final String LIBVIRT_IP_LOOKUP_STRATEGY_PROPERTY_SUFFIX = ".ipLookupStrategy";
    public static final String LIBVIRT_FS_MAPPING_SUFFIX = ".fsMapping";

    public static final String LIBVIRT_URL_DEFAULT = "qemu:///system";
    public static final String LIBVIRT_BOOT_SECONDS_DEFAULT = "60";

    private int startTimeout;
    private int bootDelay;

    private final String networkName;

    protected Connect libvirt;
    private final DomainWrapper baseDomain;
    private final String baseDomainName;

    private DomainWrapper clone;
    private String hostIp;
    private IpLookupStrategy ipLookupStrategy;

    private List<Filesystem> filesystemMappings;

    public LibvirtHost(Connect libvirt, String baseDomainName, IpLookupStrategy ipLookupStrategy, String networkName, int startTimeout, int bootDelay, List<Filesystem> filesystemMappings) {
        this.libvirt = libvirt;
        this.baseDomainName = baseDomainName;
        this.startTimeout = startTimeout;
        this.bootDelay = bootDelay;
        this.networkName = networkName;
        this.ipLookupStrategy = ipLookupStrategy;
        this.filesystemMappings = filesystemMappings;

        try {
            this.baseDomain = DomainWrapper.newWrapper(libvirt.domainLookupByName(baseDomainName));
        } catch (LibvirtException e) {
            throw new RuntimeException(e);
        }
    }

    public static IpLookupStrategy determineIpLookupStrategy(String hostLabel) {
        String strategy = getRequiredOvercastProperty(hostLabel + LIBVIRT_IP_LOOKUP_STRATEGY_PROPERTY_SUFFIX);

        if ("SSH".equals(strategy)) {
            return SshIpLookupStrategy.create(hostLabel);
        } else if ("static".equals(strategy)) {
            return StaticIpLookupStrategy.create(hostLabel);
        } else {
            throw new RuntimeException(String.format("Unsupported IP lookup strategy: '%s'", strategy));
        }
    }

    public Connect getLibvirt() {
        return libvirt;
    }

    @Override
    public void setup() {
        if (baseDomain.getState() != DomainState.VIR_DOMAIN_SHUTOFF) {
            throw new IllegalStateException(String.format("baseDomain '%s' must be shut off before it can be cloned.", baseDomainName));
        }
        clone = createClone();
        hostIp = waitUntilRunningAndGetIP(clone);
        bootDelay(bootDelay);
    }

    @Override
    public void teardown() {
        if (clone != null) {
            clone.destroyWithDisks();
            clone = null;
        }
    }

    @Override
    public String getHostName() {
        return hostIp;
    }

    @Override
    public int getPort(int port) {
        return port;
    }

    public DomainWrapper getClone() {
        return clone;
    }

    public String getBaseDomainName() {
        return baseDomainName;
    }

    protected DomainWrapper createClone() {
        String baseName = baseDomain.getName();
        String cloneName = baseName + "-" + UUID.randomUUID().toString();
        logger.info("Creating clone '{}' from base domain '{}'", cloneName, baseName);
        return baseDomain.cloneWithBackingStore(cloneName, filesystemMappings);
    }

    protected String waitUntilRunningAndGetIP(DomainWrapper clone) {
        String name = clone.getName();
        try {
            int seconds = startTimeout;
            DomainState state = DomainState.VIR_DOMAIN_NOSTATE;
            while (state != DomainState.VIR_DOMAIN_RUNNING && seconds >= 0) {
                state = clone.getState();
                logger.debug("Waiting {}s for clone '{}' to become running ({})", seconds, name, state);
                sleep(1);
                seconds--;
            }
            if (state != DomainState.VIR_DOMAIN_RUNNING) {
                String msg = String.format("Clone '%s' not running after %d seconds (state=%s)", name, startTimeout, state);
                throw new RuntimeException(msg);
            }
            logger.info("Clone '{}' running determining IP", name, startTimeout, state);

            String mac = clone.getMac(networkName);
            return ipLookupStrategy.lookup(mac);
        } catch (RuntimeException bootupFailure) {
            // something went wrong booting up or getting an IP to access the machine no point
            // in keeping it around.
            logger.error("Clone '{}' did not reach a usable state destroying. ({})", name, bootupFailure.getMessage());
            clone.destroyWithDisks();
            throw bootupFailure;
        }
    }

    protected void bootDelay(int delaySeconds) {
        logger.info("Waiting {} seconds for VM to boot up", delaySeconds);
        sleep(delaySeconds);
    }

    protected static void sleep(final int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
