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

package com.xebialabs.overcast.host;

import java.util.UUID;

import org.libvirt.Connect;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overcast.support.libvirt.DomainWrapper;
import com.xebialabs.overcast.support.libvirt.IpLookupStrategy;
import com.xebialabs.overcast.support.libvirt.SshIpLookupStrategy;

import static com.xebialabs.overcast.OvercastProperties.getOvercastProperty;
import static com.xebialabs.overcast.OvercastProperties.getRequiredOvercastProperty;

class LibvirtHost implements CloudHost {
    private static final Logger logger = LoggerFactory.getLogger(LibvirtHost.class);

    public static final String LIBVIRT_URL_PROPERTY_SUFFIX = ".libvirtURL";

    public static final String LIBVIRT_START_TIMEOUT_PROPERTY_SUFFIX = ".libvirtStartTimeout";
    public static final String LIBVIRT_START_TIMEOUT_DEFAULT = "30";

    public static final String LIBVIRT_BOOT_DELAY_PROPERTY_SUFFIX = ".libvirtBootDelay";
    public static final String LIBVIRT_BOOT_DELAY_DEFAULT = "0";

    public static final String LIBVIRT_BASE_DOMAIN_PROPERTY_SUFFIX = ".libvirtBaseDomain";
    public static final String LIBVIRT_NETWORK_DEVICE_ID_PROPERTY_SUFFIX = ".networkDeviceId";
    public static final String LIBVIRT_IP_LOOKUP_STRATEGY_PROPERTY_SUFFIX = ".ipLookupStrategy";

    public static final String LIBVIRT_URL_DEFAULT = "qemu:///system";
    public static final String LIBVIRT_BOOT_SECONDS_DEFAULT = "60";

    private String libvirtURL = null;
    private final int startTimeout;
    private int bootDelay;

    private final String networkDeviceId;

    private Connect libvirt;
    private final DomainWrapper libvirtBaseDomain;

    private DomainWrapper clone;
    private String hostIp;
    private IpLookupStrategy ipLookupStrategy;

    public LibvirtHost(String hostLabel, String libvirtBaseDomain) {
        this.libvirtURL = getOvercastProperty(hostLabel + LIBVIRT_URL_PROPERTY_SUFFIX, LIBVIRT_URL_DEFAULT);
        this.startTimeout = Integer.valueOf(getOvercastProperty(hostLabel + LIBVIRT_START_TIMEOUT_PROPERTY_SUFFIX, LIBVIRT_START_TIMEOUT_DEFAULT));
        this.bootDelay = Integer.valueOf(getOvercastProperty(hostLabel + LIBVIRT_BOOT_DELAY_PROPERTY_SUFFIX, LIBVIRT_BOOT_DELAY_DEFAULT));

        this.networkDeviceId = getOvercastProperty(hostLabel + LIBVIRT_NETWORK_DEVICE_ID_PROPERTY_SUFFIX);
        String strategy = getRequiredOvercastProperty(hostLabel + LIBVIRT_IP_LOOKUP_STRATEGY_PROPERTY_SUFFIX);
        ipLookupStrategy = determineIpLookupStrategy(hostLabel, strategy);
        try {
            this.libvirt = new Connect(libvirtURL, false);
            this.libvirtBaseDomain = DomainWrapper.newWrapper(libvirt.domainLookupByName(libvirtBaseDomain));
        } catch (LibvirtException e) {
            throw new RuntimeException(e);
        }
    }

    protected IpLookupStrategy determineIpLookupStrategy(String hostLabel, String strategy) {
        if ("SSH".equals(strategy)) {
            return SshIpLookupStrategy.create(hostLabel);
        } else {
            throw new RuntimeException(String.format("Unsupported IP lookup strategy: '%s'", strategy));
        }
    }

    @Override
    public void setup() {
        clone = createClone();
        hostIp = waitUntilRunningAndGetIP();
        bootDelay();
    }

    @Override
    public void teardown() {
        clone.destroyWithDisks();
        clone = null;
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

    protected DomainWrapper createClone() {
        String baseName = libvirtBaseDomain.getName();
        String cloneName = baseName + "-" + UUID.randomUUID();
        logger.info("Creating clone '{}' from base domain '{}'", cloneName, baseName);
        return libvirtBaseDomain.cloneWithBackingStore(cloneName);
    }

    protected String waitUntilRunningAndGetIP() {
        String name = clone.getName();
        int seconds = startTimeout;
        DomainState state = DomainState.VIR_DOMAIN_NOSTATE;
        while (state != DomainState.VIR_DOMAIN_RUNNING && seconds >= 0) {
            state = clone.getState();
            logger.debug("Waiting {}s for clone '{}' to become running ({})", seconds, name, state);
            sleep(1);
            seconds--;
        }
        if (state != DomainState.VIR_DOMAIN_RUNNING) {
            logger.error("Clone '{}' not running after {}s (state={})", name, startTimeout, state);
        } else {
            logger.info("Clone '{}' running determining IP", name, startTimeout, state);
        }
        if (networkDeviceId != null) {
            String mac = clone.getMac(networkDeviceId);
            return ipLookupStrategy.lookup(mac);
        }
        throw new RuntimeException("Unable to determine IP address for host " + name);
    }

    private void bootDelay() {
        logger.info("Waiting {} seconds for VM to boot up", bootDelay);
        sleep(bootDelay);
    }

    private static void sleep(final int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
