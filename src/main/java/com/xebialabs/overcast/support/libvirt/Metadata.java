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
package com.xebialabs.overcast.support.libvirt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import static com.xebialabs.overcast.support.libvirt.JDomUtil.getElementText;

/**
 * Utility to deal with the metadata we set on Libvirt Domains. Metadata can be for a provisioned domain or for just a clone.
 * <p>For a provisioned domain it looks like:
 * <pre>
 * &lt;metadata&gt;
 *   &lt;overcast_metdata xmlns=&quot;http://www.xebialabs.com/overcast/metadata/v1&quot;&gt;
 *     &lt;parent_domain&gt;centos6&lt;/parent_domain&gt;
 *     &lt;provisioned_with&gt;/mnt/puppet/Vagrantfile&lt;/provisioned_with&gt;
 *     &lt;provisioned_checksum&gt;2008-10-31T15:07:38.6875000-05:00&lt;/provisioned_checksum&gt;
 *     &lt;creation_time&gt;2008-10-31T15:07:38.6875000-05:00&lt;/provisioned_at&gt;
 *   &lt;/overcast_metdata&gt;
 * &lt;/metadata&gt;
 * </pre>
 * <p>For a cloned domain it looks like:
 * <pre>
 * &lt;metadata&gt;
 *   &lt;overcast_metdata xmlns=&quot;http://www.xebialabs.com/overcast/metadata/v1&quot;&gt;
 *     &lt;parent_domain&gt;centos6&lt;/parent_domain&gt;
 *     &lt;creation_time&gt;2008-10-31T15:07:38.6875000-05:00&lt;/provisioned_at&gt;
 *   &lt;/overcast_metdata&gt;
 * &lt;/metadata&gt;
 * </pre>
 */
public class Metadata {
    private static final String XML_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String METADATA_NS_V1 = "http://www.xebialabs.com/overcast/metadata/v1";
    public static final String METADATA = "metadata";
    public static final String OVERCAST_METADATA = "overcast_metadata";
    public static final String CREATION_TIME = "creation_time";
    public static final String PROVISIONED_CHECKSUM = "provisioned_checksum";
    public static final String PROVISIONED_WITH = "provisioned_with";
    public static final String PARENT_DOMAIN = "parent_domain";
    private static final TimeZone METADATA_TIMEZONE = TimeZone.getTimeZone("UTC");

    private final String parentDomain;
    private final String provisionedWith;
    private final String provisionedChecksum;
    private final Date creationTime;

    public Metadata(String parentDomain, String provisionedWith, String provisionedChecksum, Date creationTime) {
        Preconditions.checkNotNull(creationTime, "creationTime cannot be null");
        this.parentDomain = checkArgument(parentDomain, "parentDomain");
        this.provisionedWith = checkArgument(provisionedWith, "provisionedWith");
        this.provisionedChecksum = checkArgument(provisionedChecksum, "provisionedChecksum");
        this.creationTime = creationTime;
    }

    public Metadata(String parentDomain, Date creationTime) {
        Preconditions.checkNotNull(creationTime, "creationTime cannot be null");
        this.parentDomain = checkArgument(parentDomain, "parentDomain");
        this.creationTime = creationTime;
        this.provisionedWith = null;
        this.provisionedChecksum = null;
    }

    private static String checkArgument(String arg, String argName) {
        Preconditions.checkArgument(arg != null && !arg.isEmpty(), "%s cannot be null or empty", argName);
        return arg;
    }

    public String getParentDomain() {
        return parentDomain;
    }

    public String getProvisionedWith() {
        return provisionedWith;
    }

    public String getProvisionedChecksum() {
        return provisionedChecksum;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public boolean isProvisioned() {
        return provisionedWith != null;
    }

    /**
     * Extract {@link Metadata} from the domain XML. Throws {@link IllegalArgumentException} if the metadata is
     * malformed.
     *
     * @return the metadata or <code>null</code> if there's no metadata
     */
    public static Metadata fromXml(Document domainXml) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(XML_DATE_FORMAT);
            Element metadata = getMetadataElement(domainXml);
            if (metadata == null) {
                return null;
            }
            Namespace ns = Namespace.getNamespace(METADATA_NS_V1);
            Element ocMetadata = metadata.getChild(OVERCAST_METADATA, ns);
            if (ocMetadata == null) {
                return null;
            }
            String parentDomain = getElementText(ocMetadata, PARENT_DOMAIN, ns);
            String creationTime = getElementText(ocMetadata, CREATION_TIME, ns);
            Date date = sdf.parse(creationTime);

            if(ocMetadata.getChild(PROVISIONED_WITH, ns) != null) {
                String provisionedWith = getElementText(ocMetadata, PROVISIONED_WITH, ns);
                String checkSum = getElementText(ocMetadata, PROVISIONED_CHECKSUM, ns);

                return new Metadata(parentDomain, provisionedWith, checkSum, date);
            }
            return new Metadata(parentDomain, date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date in metadata on domain", e);
        }
    }

    private static Element createProvisioningMetadata(String parentDomain, String provisionedWith, String provisionedChecksum, Date provisionedAt) {
        SimpleDateFormat sdf = new SimpleDateFormat(XML_DATE_FORMAT);
        sdf.setTimeZone(METADATA_TIMEZONE);

        Element metadata = new Element(METADATA);
        Element ocmetadata = new Element(OVERCAST_METADATA, METADATA_NS_V1);
        metadata.addContent(ocmetadata);

        ocmetadata.addContent(new Element(PARENT_DOMAIN, METADATA_NS_V1).setText(parentDomain));
        ocmetadata.addContent(new Element(PROVISIONED_WITH, METADATA_NS_V1).setText(provisionedWith));
        ocmetadata.addContent(new Element(PROVISIONED_CHECKSUM, METADATA_NS_V1).setText(provisionedChecksum));
        ocmetadata.addContent(new Element(CREATION_TIME, METADATA_NS_V1).setText(sdf.format(provisionedAt)));

        return metadata;
    }

    private static Element createCloningMetadata(String parentDomain, Date creationTime) {
        SimpleDateFormat sdf = new SimpleDateFormat(XML_DATE_FORMAT);
        sdf.setTimeZone(METADATA_TIMEZONE);

        Element metadata = new Element(METADATA);
        Element ocmetadata = new Element(OVERCAST_METADATA, METADATA_NS_V1);
        metadata.addContent(ocmetadata);

        ocmetadata.addContent(new Element(PARENT_DOMAIN, METADATA_NS_V1).setText(parentDomain));
        ocmetadata.addContent(new Element(CREATION_TIME, METADATA_NS_V1).setText(sdf.format(creationTime)));

        return metadata;
    }

    private static Element getMetadataElement(Document domainXml) {
        Element metadata = domainXml.getRootElement().getChild(METADATA);
        if (metadata == null) {
            return null;
        }
        return metadata;
    }

    public static void updateProvisioningMetadata(Document domainXml, String baseDomainName, String provisionCmd, String expirationTag, Date creationTime) {
        checkArgument(baseDomainName, "baseDomainName");
        checkArgument(provisionCmd, "provisionCmd");
        checkArgument(expirationTag, "expirationTag");
        Preconditions.checkNotNull(creationTime, "creationTime must not be null");

        Element element = getMetadataElement(domainXml);
        if (element != null) {
            domainXml.getRootElement().removeContent(element);
        }
        Element metadata = createProvisioningMetadata(baseDomainName, provisionCmd, expirationTag, creationTime);
        domainXml.getRootElement().addContent(metadata);
    }

    public static void updateCloneMetadata(Document domainXml, String baseDomainName, Date creationTime) {
        checkArgument(baseDomainName, "baseDomainName");
        Preconditions.checkNotNull(creationTime, "creationTime must not be null");

        Element element = getMetadataElement(domainXml);
        if (element != null) {
            domainXml.getRootElement().removeContent(element);
        }
        Element metadata = createCloningMetadata(baseDomainName, creationTime);
        domainXml.getRootElement().addContent(metadata);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("parentDomain", parentDomain)
            .add("creationTime", creationTime)
            .add("provisionedChecksum", provisionedChecksum)
            .add("provisionedWith", provisionedWith).toString();
    }
}
