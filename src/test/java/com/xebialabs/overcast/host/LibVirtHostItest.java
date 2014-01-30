package com.xebialabs.overcast.host;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.libvirt.DomainInfo.DomainState;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeNotNull;

public class LibVirtHostItest {

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

    public LibvirtHost getCloudHost(String name) {
        try {
            return (LibvirtHost) CloudHostFactory.getCloudHost(name);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Test
    public void shouldBootHostWithSshIpLookupStrategy() {
        LibvirtHost itestHost = getCloudHost("overcastLibVirtItestDhcpHost");
        assumeNotNull(itestHost);

        try {
            itestHost.setup();

            assertThat(itestHost.getHostName(), matchesPattern("\\d+\\.\\d+\\.\\d+\\.\\d+"));
            assertThat(itestHost.getClone(), notNullValue());
            assertThat(itestHost.getClone().getState(), equalTo(DomainState.VIR_DOMAIN_RUNNING));
        } finally {
            itestHost.teardown();
            assertThat(itestHost.getClone(), nullValue());
        }
    }

    @Test
    public void shouldBootHostWithStaticIp() {
        LibvirtHost itestHost = getCloudHost("overcastItestStaticIpHost");
        assumeNotNull(itestHost);

        try {
            itestHost.setup();

            assertThat(itestHost.getHostName(), matchesPattern("\\d+\\.\\d+\\.\\d+\\.\\d+"));
            assertThat(itestHost.getClone(), notNullValue());
            assertThat(itestHost.getClone().getState(), equalTo(DomainState.VIR_DOMAIN_RUNNING));
        } finally {
            itestHost.teardown();
            assertThat(itestHost.getClone(), nullValue());
        }
    }
}
