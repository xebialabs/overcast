/**
 *    Copyright 2012-2020 XebiaLabs B.V.
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
package com.xebialabs.overcast.support.libvirt;

import java.util.Date;
import org.jdom2.Document;
import org.junit.Test;

import static com.xebialabs.overcast.support.libvirt.JDomUtil.documentToRawString;
import static com.xebialabs.overcast.support.libvirt.JDomUtil.stringToDocument;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class MetadataTest {
    private static final String XML_WITHOUT_METADATA = "<domain type='kvm'>"
            + "<name>centos6</name>"
            + "<uuid>e5905e8d-4698-2b41-59a7-f4a98d9aa61e</uuid>"
            + "<memory unit='KiB'>1048576</memory>"
            + "</domain>";

    private static final String XML_WITH_METADATA = "<domain type=\"kvm\">"
            + "<name>centos6</name>"
            + "<uuid>e5905e8d-4698-2b41-59a7-f4a98d9aa61e</uuid>"
            + "<memory unit=\"KiB\">1048576</memory>"
            + "<metadata>"
            + "<overcast_metadata xmlns=\"http://www.xebialabs.com/overcast/metadata/v1\">"
            + "<parent_domain>basedom</parent_domain>"
            + "<provisioned_with>provcmd</provisioned_with>"
            + "<provisioned_checksum>expire</provisioned_checksum>"
            + "<creation_time>1970-01-01T00:00:00Z</creation_time>"
            + "</overcast_metadata>"
            + "</metadata>"
            + "</domain>";

    private static final String EXPECTED_XML_WITH_METADATA = "<domain type=\"kvm\">"
        + "<name>centos6</name>"
        + "<uuid>e5905e8d-4698-2b41-59a7-f4a98d9aa61e</uuid>"
        + "<memory unit=\"KiB\">1048576</memory>"
        + "<metadata>"
        + "<overcast_metadata xmlns=\"http://www.xebialabs.com/overcast/metadata/v1\">"
        + "<parent_domain>basedom2</parent_domain>"
        + "<provisioned_with>provcmd2</provisioned_with>"
        + "<provisioned_checksum>expire2</provisioned_checksum>"
        + "<creation_time>2009-02-13T23:31:31Z</creation_time>"
        + "</overcast_metadata>"
        + "</metadata>"
        + "</domain>";

    private static final String EXPECTED_XML_WITH_CLONE_METADATA = "<domain type=\"kvm\">"
        + "<name>centos6</name>"
        + "<uuid>e5905e8d-4698-2b41-59a7-f4a98d9aa61e</uuid>"
        + "<memory unit=\"KiB\">1048576</memory>"
        + "<metadata>"
        + "<overcast_metadata xmlns=\"http://www.xebialabs.com/overcast/metadata/v1\">"
        + "<parent_domain>basedom2</parent_domain>"
        + "<creation_time>2009-02-13T23:31:31Z</creation_time>"
        + "</overcast_metadata>"
        + "</metadata>"
        + "</domain>";

    @Test
    public void shouldUpdateXmlWithoutMetadataTag() throws Exception {
        Document doc = stringToDocument(XML_WITHOUT_METADATA);
        Metadata.updateProvisioningMetadata(doc, "basedom", "provcmd", "expire", new Date(0));
        String val = documentToRawString(doc);
        assertThat(XML_WITH_METADATA, equalTo(val));
    }

    @Test
    public void shouldUpdateXmlWithMetadataTag() throws Exception {
        Document doc = stringToDocument(XML_WITH_METADATA);
        Metadata.updateProvisioningMetadata(doc, "basedom2", "provcmd2", "expire2", new Date(1234567891234L));
        String val = documentToRawString(doc);
        assertThat(val, equalTo(EXPECTED_XML_WITH_METADATA));
    }

    @Test
    public void shouldUpdateXmlWithCloneMetadataTag() throws Exception {
        Document doc = stringToDocument(XML_WITH_METADATA);
        Metadata.updateCloneMetadata(doc, "basedom2", new Date(1234567891234L));
        String val = documentToRawString(doc);
        assertThat(val, equalTo(EXPECTED_XML_WITH_CLONE_METADATA));
    }

    @Test
    public void shouldReturnNullWhenNoMetadata() {
        Document xml = stringToDocument("<domain type='kvm'>"
            + "<name>centos6</name>"
            + "<uuid>e5905e8d-4698-2b41-59a7-f4a98d9aa61e</uuid>"
            + "<memory unit='KiB'>1048576</memory>"
            + "</domain>");
        assertThat(Metadata.fromXml(xml), nullValue());
    }

    @Test
    public void shouldReturnCloneMetadata() {
        Document xml = stringToDocument("<domain type='kvm'>"
            + "<name>centos6</name>"
            + "<uuid>e5905e8d-4698-2b41-59a7-f4a98d9aa61e</uuid>"
            + "<memory unit='KiB'>1048576</memory>"
            + "<metadata>"
            + "<overcast_metadata xmlns=\"http://www.xebialabs.com/overcast/metadata/v1\">"
            + "<parent_domain>basedom2</parent_domain>"
            + "<creation_time>2009-02-14T00:31:31Z</creation_time>"
            + "</overcast_metadata>"
            + "</metadata>"
            + "</domain>");
        Metadata metadata = Metadata.fromXml(xml);
        assertThat(metadata, notNullValue());
        assertThat(metadata.isProvisioned(), equalTo(false));
        assertThat(metadata.getCreationTime(), notNullValue());
        assertThat(metadata.getParentDomain(), equalTo("basedom2"));
        assertThat(metadata.getProvisionedWith(), nullValue());
        assertThat(metadata.getProvisionedChecksum(), nullValue());
    }

    @Test
    public void shouldReturnProvisionedMetadata() {
        Document xml = stringToDocument("<domain type='kvm'>"
            + "<name>centos6</name>"
            + "<uuid>e5905e8d-4698-2b41-59a7-f4a98d9aa61e</uuid>"
            + "<memory unit='KiB'>1048576</memory>"
            + "<metadata>"
            + "<overcast_metadata xmlns=\"http://www.xebialabs.com/overcast/metadata/v1\">"
            + "<parent_domain>basedom2</parent_domain>"
            + "<provisioned_with>provcmd2</provisioned_with>"
            + "<provisioned_checksum>expire2</provisioned_checksum>"
            + "<creation_time>2009-02-14T00:31:31Z</creation_time>"
            + "</overcast_metadata>"
            + "</metadata>"
            + "</domain>");
        Metadata metadata = Metadata.fromXml(xml);
        assertThat(metadata, notNullValue());
        assertThat(metadata.isProvisioned(), equalTo(true));
        assertThat(metadata.getCreationTime(), notNullValue());
        assertThat(metadata.getParentDomain(), equalTo("basedom2"));
        assertThat(metadata.getProvisionedWith(), equalTo("provcmd2"));
        assertThat(metadata.getProvisionedChecksum(), equalTo("expire2"));
    }

}
