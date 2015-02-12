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

import org.junit.Test;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class CachedLibvirtHostTest {
    static final String DOMAIN_XML= "<domain type='kvm'></domain>";
    static final String STALE_DOMAIN_XML= "<domain type='kvm'>"
        +"<metadata>"
        + "<overcast_metadata xmlns=\"http://www.xebialabs.com/overcast/metadata/v1\">"
        + "<parent_domain>parentDomain</parent_domain>"
        + "<provisioned_with>provcmd</provisioned_with>"
        + "<provisioned_checksum>expire</provisioned_checksum>"
        + "<creation_time>2014-06-13T12:28:49Z</creation_time>"
        + "</overcast_metadata>"
        + "</metadata>"
        + "</domain>";

    static final String CLONE_OF_STALE_DOMAIN_XML= "<domain type='kvm'>"
        +"<metadata>"
        + "<overcast_metadata xmlns=\"http://www.xebialabs.com/overcast/metadata/v1\">"
        + "<parent_domain>staleDomain</parent_domain>"
        + "<creation_time>2014-06-13T12:28:49Z</creation_time>"
        + "</overcast_metadata>"
        + "</metadata>"
        + "</domain>";

    static final String CLONE_OF_UNRELATED_DOMAIN_XML= "<domain type='kvm'>"
        +"<metadata>"
        + "<overcast_metadata xmlns=\"http://www.xebialabs.com/overcast/metadata/v1\">"
        + "<parent_domain>someOtherParent</parent_domain>"
        + "<creation_time>2014-06-13T12:28:49Z</creation_time>"
        + "</overcast_metadata>"
        + "</metadata>"
        + "</domain>";

    @Test
    public void shouldReturnTrueIfNoRunningDomains() throws LibvirtException {
        Connect libvirt = Mockito.mock(Connect.class);
        Domain staleDomain = Mockito.mock(Domain.class);
        when(staleDomain.getName()).thenReturn("stale");

        // when no running domains
        when(libvirt.listDomains()).thenReturn(new int[] {});

        assertThat(CachedLibvirtHost.isDomainSafeToDelete(libvirt, "staleDomain"), equalTo(true));
    }

    @Test
    public void shouldReturnFalseIfRunningClonePresent() throws LibvirtException {
        Connect libvirt = Mockito.mock(Connect.class);
        Domain staleDomain = Mockito.mock(Domain.class);

        when(staleDomain.getXMLDesc(0)).thenReturn(STALE_DOMAIN_XML);
        when(staleDomain.getName()).thenReturn("stale");

        Domain cloneOfStale = Mockito.mock(Domain.class);
        when(cloneOfStale.getXMLDesc(0)).thenReturn(CLONE_OF_STALE_DOMAIN_XML);
        when(cloneOfStale.getName()).thenReturn("cloneOfStaleDomain");

        // when a running domain that is a stale clone
        when(libvirt.listDomains()).thenReturn(new int[] { 1 });
        when(libvirt.domainLookupByID(1)).thenReturn(cloneOfStale);

        assertThat(CachedLibvirtHost.isDomainSafeToDelete(libvirt, "staleDomain"), equalTo(false));
    }

    @Test
    public void shouldReturnFalseIfRunningDomains() throws LibvirtException {
        Connect libvirt = Mockito.mock(Connect.class);
        Domain staleDomain = Mockito.mock(Domain.class);

        when(staleDomain.getXMLDesc(0)).thenReturn(STALE_DOMAIN_XML);
        when(staleDomain.getName()).thenReturn("stale");

        Domain unrelatedClone = Mockito.mock(Domain.class);
        when(unrelatedClone.getXMLDesc(0)).thenReturn(CLONE_OF_UNRELATED_DOMAIN_XML);
        when(unrelatedClone.getName()).thenReturn("cloneOfStaleDomain");

        // when a running domain that is not a clone of the domain
        when(libvirt.listDomains()).thenReturn(new int[] { 1 });
        when(libvirt.domainLookupByID(1)).thenReturn(unrelatedClone);

        assertThat(CachedLibvirtHost.isDomainSafeToDelete(libvirt, "staleDomain"), equalTo(true));
    }
}
