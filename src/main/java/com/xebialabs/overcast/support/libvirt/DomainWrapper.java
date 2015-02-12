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

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.libvirt.StorageVol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import static com.xebialabs.overcast.support.libvirt.JDomUtil.documentToString;
import static com.xebialabs.overcast.support.libvirt.Metadata.updateCloneMetadata;
import static com.xebialabs.overcast.support.libvirt.Metadata.updateProvisioningMetadata;
import static com.xebialabs.overcast.support.libvirt.jdom.DiskXml.getDisks;
import static com.xebialabs.overcast.support.libvirt.jdom.DiskXml.updateDisks;
import static com.xebialabs.overcast.support.libvirt.jdom.DomainXml.prepareForCloning;
import static com.xebialabs.overcast.support.libvirt.jdom.DomainXml.setDomainName;
import static com.xebialabs.overcast.support.libvirt.jdom.FilesystemXml.getFilesystems;
import static com.xebialabs.overcast.support.libvirt.jdom.FilesystemXml.removeFilesystemsWithTarget;
import static com.xebialabs.overcast.support.libvirt.jdom.FilesystemXml.toFileSystemXml;
import static com.xebialabs.overcast.support.libvirt.jdom.InterfaceXml.getMacs;

public class DomainWrapper {
    private static final Logger logger = LoggerFactory.getLogger(DomainWrapper.class);
    private Document domainXml;
    private Domain domain;

    public DomainWrapper(Domain domain, Document domainXml) {
        this.domain = domain;
        this.domainXml = domainXml;
    }

    public String getName() {
        try {
            return domain.getName();
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException("Unable to get domain name", e);
        }
    }

    public void reloadDomainXml() throws LibvirtException {
        domainXml = LibvirtUtil.loadDomainXml(domain);
    }

    public static DomainWrapper newWrapper(Domain domain) {
        return new DomainWrapper(domain, LibvirtUtil.loadDomainXml(domain));
    }

    public void destroyWithDisks() {
        try {
            // look up state before undefining the domain...
            boolean isRunning = (domain.getInfo().state == DomainState.VIR_DOMAIN_RUNNING);
            List<Disk> disks = getDisks(domain.getConnect(), domainXml);
            logger.info("Undefining domain '{}'", domain.getName());
            domain.undefine(3); // also remove snapshot data and managed save data

            if (isRunning) {
                logger.info("Shutting down domain '{}'", domain.getName());
                domain.destroy();
            }

            // this will not destroy the backing store disks.
            for (Disk disk : disks) {
                logger.info("Removing disk {}", disk.getName());
                disk.getVolume().delete(0);
            }
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException("Unable to destroy domain", e);
        }
    }

    public DomainState getState() {
        try {
            return domain.getInfo().state;
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException("Unable to get domain state", e);
        }
    }

    public String getMac(String id) {
        if(id == null) {
            return null;
        }
        return getMacs(domainXml).get(id);
   }

    /**
     * Clone the domain. All disks are cloned using the original disk as backing store. The names of the disks are
     * created by suffixing the original disk name with a number.
     */
    public DomainWrapper cloneWithBackingStore(String cloneName, List<Filesystem> mappings) {
        logger.info("Creating clone from {}", getName());
        try {
            // duplicate definition of base
            Document cloneXmlDocument = domainXml.clone();

            setDomainName(cloneXmlDocument, cloneName);

            prepareForCloning(cloneXmlDocument);

            // keep track of who we are a clone from...
            updateCloneMetadata(cloneXmlDocument, getName(), new Date());

            cloneDisks(cloneXmlDocument, cloneName);

            updateFilesystemMappings(cloneXmlDocument, mappings);

            String cloneXml = documentToString(cloneXmlDocument);
            logger.debug("Clone xml={}", cloneXml);

            // Domain cloneDomain = domain.getConnect().domainCreateXML(cloneXml, 0);
            Domain cloneDomain = domain.getConnect().domainDefineXML(cloneXml);
            String createdCloneXml = cloneDomain.getXMLDesc(0);
            logger.debug("Created clone xml: {}", createdCloneXml);
            cloneDomain.create();
            logger.debug("Starting clone: '{}'", cloneDomain.getName());

            DomainWrapper clone = newWrapper(cloneDomain);
            return clone;
        } catch (IOException e) {
            throw new LibvirtRuntimeException("Unable to clone domain", e);
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException("Unable to clone domain", e);
        }
    }

    private void updateFilesystemMappings(Document cloneXmlDocument, List<Filesystem> mappings) {
        Element devices = cloneXmlDocument.getRootElement().getChild("devices");

        Map<String, Filesystem> currentFilesystems = getFilesystems(domainXml);
        for (Filesystem fs : mappings) {
            if (currentFilesystems.containsKey(fs.target)) {
                removeFilesystemsWithTarget(cloneXmlDocument, fs.target);
            }
            devices.addContent(toFileSystemXml(fs));
        }
    }

    private void cloneDisks(Document cloneXmlDocument, String cloneName) throws LibvirtException {
        List<StorageVol> cloneDisks = Lists.newArrayList();
        int idx = 0;
        for (Disk d : getDisks(domain.getConnect(), domainXml)) {
            idx++;
            String clonedDisk = String.format("%s-%02d.qcow2", cloneName, idx);
            StorageVol vol = d.createCloneWithBackingStore(clonedDisk);
            logger.debug("Disk {} cloned to {}", d.getName(), clonedDisk);
            cloneDisks.add(vol);
        }
        updateDisks(cloneXmlDocument, cloneDisks);
    }

    public DomainWrapper cloneWithBackingStore(String cloneName) {
        return cloneWithBackingStore(cloneName, Collections.<Filesystem> emptyList());
    }

    public Document getDomainXml() {
        return domainXml;
    }

    public void updateMetadata(String baseDomainName, String provisionCmd, String expirationTag, Date date) {
        try {
            if (domain.isActive() == 1) {
                throw new IllegalStateException("Domain must be shut down before updating metdata");
            }
            // need a really fresh copy of the domain xml or the update may fail...
            reloadDomainXml();
            updateProvisioningMetadata(domainXml, baseDomainName, provisionCmd, expirationTag, date);
            String xml = documentToString(domainXml);
            logger.debug("Updating domain '{}' XML with {}", getName(), xml);
            domain.getConnect().domainDefineXML(xml);
        } catch (IOException e) {
            throw new LibvirtRuntimeException(String.format("Unable to update metadata for domain '%s'", getName()), e);
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(String.format("Unable to update metadata for domain '%s'", getName()), e);
        }
    }

    public void acpiShutdown() {
        logger.info("Shutting down domain '{}'", getName());
        try {
            domain.shutdown();

            while (this.domain.isActive() == 1) {
                sleep(1);
            }
            logger.debug("Domain '{}' shut down (active={})", getName(), this.domain.isActive());
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(String.format("Unable to shut down domain '%s'", getName()), e);
        }
    }

    private static void sleep(final int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
