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

import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jdom2.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

import com.xebialabs.overcast.OvercastProperties;
import com.xebialabs.overcast.support.libvirt.DomainWrapper;
import com.xebialabs.overcast.support.libvirt.LibvirtUtil;
import com.xebialabs.overcast.support.libvirt.Metadata;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/* NOTE: there should only be one of this test running on a KVM instance. */
public class LibVirtHostItest {
    private static final Logger logger = LoggerFactory.getLogger(LibVirtHostItest.class);

    public static class RegexMatcher extends BaseMatcher<String> {
        private final String regex;

        public RegexMatcher(String regex) {
            this.regex = regex;
        }

        public boolean matches(Object o) {
            return ((String) o).matches(regex);
        }

        public void describeTo(Description description) {
            description.appendText("matches regex=");
        }

        public static RegexMatcher matches(String regex) {
            return new RegexMatcher(regex);
        }
    }

    public static BaseMatcher<String> matchesPattern(String pattern) {
        return new RegexMatcher(pattern);
    }

    private Connect libvirt;

    public CloudHost getCloudHost(String name) {
        try {
            return CloudHostFactory.getCloudHost(name);
        } catch (IllegalStateException e) {
            logger.warn("Host not found", e);
            return null;
        }
    }

    public static List<Domain> findCachedDomains(List<Domain> domains, Set<String> baseBoxes) {
        List<Domain> cached = newArrayList();
        for (Domain domain : domains) {
            Document definition = LibvirtUtil.loadDomainXml(domain);
            Metadata md = Metadata.fromXml(definition);
            if (md == null) {
                continue;
            }
            if (baseBoxes.contains(md.getParentDomain())) {
                cached.add(domain);
            }
        }
        return cached;
    }

    // check that nothing is running/defined
    @BeforeClass
    public static void checkKvm() throws LibvirtException {
        logger.info("Checking if KVM host is 'clean'");
        final String libvirtUrl = OvercastProperties.getRequiredOvercastProperty("overcastItest.libvirtUrl");
        final String basebox = OvercastProperties.getRequiredOvercastProperty("overcastItest.basebox");
        final String staticBaseBox = OvercastProperties.getRequiredOvercastProperty("overcastItest.staticbasebox");
        final String windowsBaseBox = OvercastProperties.getRequiredOvercastProperty("overcastItest.windowsbasebox");
        Set<String> baseBoxes = Sets.newHashSet(basebox, staticBaseBox, windowsBaseBox);

        Connect libvirt = null;

        try {
            libvirt = new Connect(libvirtUrl);
            List<Domain> defined = LibvirtUtil.getDefinedDomains(libvirt);
            List<Domain> running = LibvirtUtil.getRunningDomains(libvirt);
            List<Domain> all = newArrayList();
            all.addAll(defined);
            all.addAll(running);
            List<Domain> cached = findCachedDomains(all, baseBoxes);
            if (!cached.isEmpty()) {
                List<String> names = transform(cached, new Function<Domain, String>() {
                    @Override
                    public String apply(Domain domain) {
                        try {
                            return domain.getName();
                        } catch (LibvirtException e) {
                            throw new RuntimeException("Error getting domain names.", e);
                        }
                    }
                });
                throw new RuntimeException("Still cached domains present: " + names);
            }
        } finally {
            if (libvirt != null) {
                libvirt.close();
            }
        }
        logger.info("KVM host is ok for tests!");
    }

    @Before
    public void setup() throws LibvirtException {
        final String libvirtUrl = OvercastProperties.getRequiredOvercastProperty("overcastItest.libvirtUrl");
        libvirt = new Connect(libvirtUrl);
    }

    @After
    public void teardown() throws LibvirtException {
        if (libvirt != null) {
            libvirt.close();
        }
    }

    public List<Domain> findCached(String base) throws LibvirtException {
        List<Domain> defined = LibvirtUtil.getDefinedDomains(libvirt);
        List<Domain> cached = findCachedDomains(defined, Sets.newHashSet(base));
        return cached;
    }

    public Domain findDomain(String name) {
        try {
            return libvirt.domainLookupByName(name);
        } catch (LibvirtException e) {
            logger.debug("Error looking up domain '{}' assuming it doesn't exist: {}", name, e.getMessage());
            return null;
        }
    }

    @Test
    public void shouldBootHostWithStaticIp() {
        LibvirtHost itestHost = (LibvirtHost) getCloudHost("overcastItestStaticIpHost");
        assertThat(itestHost, notNullValue());

        try {
            itestHost.setup();

            assertThat(itestHost.getHostName(), matchesPattern("\\d+\\.\\d+\\.\\d+\\.\\d+"));
            assertThat(itestHost.getClone(), notNullValue());
            assertThat(itestHost.getClone().getState(), equalTo(DomainState.VIR_DOMAIN_RUNNING));
        } finally {
            itestHost.teardown();
        }
    }

    @Test
    public void shouldBootDynamicHostNoCache() throws LibvirtException {
        cachedHostNotCachedTest("overcastLibVirtItestBridgedProvisionedDhcpHost");
    }

    // depends on previous test (e.g. a cached host is expected)
    @Test
    public void shouldBootDynamicHostWithCache() throws LibvirtException {
        cachedHostCachedTest("overcastLibVirtItestBridgedProvisionedDhcpHost");
    }

    @Test
    public void shouldBootStaticHostNoCache() throws LibvirtException {
        cachedHostNotCachedTest("overcastItestProvisionedStaticIpHost");
    }

    // depends on previous test (e.g. a cached host is expected)
    @Test
    public void shouldBootStaticHostWithCache() throws LibvirtException {
        cachedHostCachedTest("overcastItestProvisionedStaticIpHost");
    }

    @Test
    public void shouldDetectFailedProvisioningOnExitCode() throws LibvirtException {
        failedProvisonTest("overcastLibVirtItestBridgedDhcpHostFailedOnExit", "exit code");
    }

    @Test
    public void shouldBootDynamicWindowsHostNoCache() throws LibvirtException {
        cachedHostNotCachedTest("overcastLibVirtItestWindows");
    }

    // depends on previous test (e.g. a cached host is expected)
    @Test
    public void shouldBootDynamicWindowsHostWithCache() throws LibvirtException {
        cachedHostCachedTest("overcastLibVirtItestWindows");
    }

    @Test
    public void shouldCreateHostWithFsMapping() throws LibvirtException {
        CachedLibvirtHost itestHost = (CachedLibvirtHost) getCloudHost("overcastLibVirtItestHostWithFsMapping");
        assertThat(itestHost, notNullValue());
        final String baseName = itestHost.getBaseDomainName();
        String cacheName = "unset";

        try {
            itestHost.setup();

            // the test succeeds if the provisioning succeeds
            // since the mounting will fail if the mapping is not done

            List<Domain> cached = findCached(baseName);
            assertThat(cached, hasSize(1));
            cacheName = cached.get(0).getName();

            assertThat(itestHost.getHostName(), matchesPattern("\\d+\\.\\d+\\.\\d+\\.\\d+"));
        } finally {
            itestHost.teardown();
        }

        assertThat("Cache name should be initialized", cacheName, not(equalTo("unset")));
        Domain cache = findDomain(cacheName);

        // clean up after ourselves
        DomainWrapper.newWrapper(cache).destroyWithDisks();
    }


    // disabled: stderr doesn't seem to work @Test
    public void shouldDetectFailedProvisioningOnStdErr() throws LibvirtException {
        failedProvisonTest("overcastLibVirtItestBridgedDhcpHostFailedOnStdErr", "output to stderr");
    }

    private void failedProvisonTest(String host, String expectedMessage) throws LibvirtException {
        CachedLibvirtHost itestHost = (CachedLibvirtHost) getCloudHost(host);
        assertThat(itestHost, notNullValue());
        final String baseName = itestHost.getBaseDomainName();

        List<Domain> cached = findCached(baseName);
        assertThat(cached, hasSize(0));

        try {
            itestHost.setup();
            Assert.fail("Host should have failed setup!");
        } catch (RuntimeException e) {
            // bad style... have to clean up exceptions coming out of the library
            assertThat(e.getMessage(), containsString(expectedMessage));

            // ensure there's no clones running of the base box
            List<Domain> running = LibvirtUtil.getRunningDomains(libvirt);
            final String basebox = OvercastProperties.getRequiredOvercastProperty("overcastItest.basebox");
            List<Domain> clones = findCachedDomains(running, Sets.newHashSet(basebox));
            assertThat("There should not be a partially provisioned clone around.", clones, hasSize(0));
        } finally {
            itestHost.teardown();
        }
    }

    private void cachedHostCachedTest(String hostKey) throws LibvirtException {
        CachedLibvirtHost itestHost = (CachedLibvirtHost) getCloudHost(hostKey);
        assertThat(itestHost, notNullValue());
        final String baseName = itestHost.getBaseDomainName();

        List<Domain> cached = findCached(baseName);
        assertThat(cached, hasSize(1));

        String cacheName = cached.get(0).getName();

        String cloneName = "unset";

        try {
            itestHost.setup();

            cloneName = itestHost.getClone().getName();

            assertThat(itestHost.getHostName(), matchesPattern("\\d+\\.\\d+\\.\\d+\\.\\d+"));

            assertThat(itestHost.getClone(), notNullValue());
            assertThat(itestHost.getClone().getState(), equalTo(DomainState.VIR_DOMAIN_RUNNING));
        } finally {
            itestHost.teardown();
        }

        Domain base = findDomain(baseName);
        assertThat("Base domain should be untouched", base, notNullValue());

        assertThat("Cache name should be initialized", cacheName, not(equalTo("unset")));
        Domain cache = findDomain(cacheName);
        assertThat("Cache should be present", cache, notNullValue());

        assertThat("Clone name should be initialized", cloneName, not(equalTo("unset")));

        Domain dom = findDomain(cloneName);
        assertThat("Clone should be gone", dom, nullValue());

        // clean up after ourselves
        DomainWrapper.newWrapper(cache).destroyWithDisks();
    }

    private void cachedHostNotCachedTest(String host) throws LibvirtException {
        CachedLibvirtHost itestHost = (CachedLibvirtHost) getCloudHost(host);
        assertThat(itestHost, notNullValue());
        final String baseName = itestHost.getBaseDomainName();

        assertThat(findCached(baseName), hasSize(0));

        String cacheName = "unset";
        String cloneName = "unset";

        try {
            itestHost.setup();

            List<Domain> cached = findCached(baseName);
            assertThat(cached, hasSize(1));
            Domain cache = cached.get(0);

            assertThat("Cache is present", cache, notNullValue());
            assertThat("Cache should be shut off", cache.getInfo().state, equalTo(DomainState.VIR_DOMAIN_SHUTOFF));

            cacheName = cache.getName();

            cloneName = itestHost.getClone().getName();

            assertThat(itestHost.getHostName(), matchesPattern("\\d+\\.\\d+\\.\\d+\\.\\d+"));

            assertThat(itestHost.getClone(), notNullValue());
            assertThat(itestHost.getClone().getState(), equalTo(DomainState.VIR_DOMAIN_RUNNING));
        } finally {
            itestHost.teardown();
        }

        Domain base = findDomain(baseName);
        assertThat("Base domain should be untouched", base, notNullValue());

        assertThat("Cache name should be initialized", cacheName, not(equalTo("unset")));
        Domain cache = findDomain(cacheName);
        assertThat("Cache should be present", cache, notNullValue());

        assertThat("Clone name should be initialized", cloneName, not(equalTo("unset")));

        Domain dom = findDomain(cloneName);
        assertThat("Clone should be gone", dom, nullValue());
    }
}
