package com.xebialabs.overcast.support.libvirt;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.libvirt.StorageVol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.xebialabs.overcast.support.libvirt.JDomUtil.documentToString;
import static com.xebialabs.overcast.support.libvirt.Metadata.updateCloneMetadata;
import static com.xebialabs.overcast.support.libvirt.Metadata.updateProvisioningMetadata;

public class DomainWrapper {
    private static final String XPATH_DISK_DEV = "//target/@dev";
    private static final String XPATH_DISK_FILE = "//source/@file";
    private static final String XPATH_DISK_TYPE = "//driver[@name='qemu']/@type";
    private static final String XPATH_DISK = "/domain/devices/disk[@device='disk']";
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
            List<Disk> disks = getDisks();
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

    /**
     * Get a map of mac addresses of interfaces defined on the domain. This is somewhat limited at the moment. It is
     * assumed that only one network interface with mac is connected to a bridge or network. For instance if you have a
     * bridged network device connected to 'br0' then you will find it's MAC address with the key 'br0'.
     */
    public Map<String, String> getMacs() {
        Map<String, String> macs = Maps.newHashMap();
        XPathFactory xpf = XPathFactory.instance();
        XPathExpression<Element> interfaces = xpf.compile("/domain/devices/interface", Filters.element());
        for (Element iface : interfaces.evaluate(domainXml)) {
            String interfaceType = iface.getAttribute("type").getValue();
            logger.debug("Detecting IP on network of type '{}'", interfaceType);
            if ("bridge".equals(interfaceType)) {
                Element macElement = iface.getChild("mac");
                String mac = macElement.getAttribute("address").getValue();
                Element sourceElement = iface.getChild("source");
                String bridge = sourceElement.getAttribute("bridge").getValue();
                logger.info("Detected MAC '{}' on bridge '{}'", mac, bridge);
                macs.put(bridge, mac);
            } else if ("network".equals(interfaceType)) {
                Element macElement = iface.getChild("mac");
                String mac = macElement.getAttribute("address").getValue();
                Element sourceElement = iface.getChild("source");
                String network = sourceElement.getAttribute("network").getValue();
                logger.info("Detected MAC '{}' on network '{}'", mac, network);
                macs.put(network, mac);
            } else {
                logger.warn("Ignoring network of type {}", interfaceType);
            }
        }
        return macs;
    }

    public String getMac(String id) {
        if(id == null) {
            return null;
        }
        return getMacs().get(id);
    }

    /** get the disks connected to this domain. */
    public List<Disk> getDisks() {
        try {
            List<Disk> ret = Lists.newArrayList();
            XPathFactory xpf = XPathFactory.instance();
            XPathExpression<Element> diskExpr = xpf.compile(XPATH_DISK, Filters.element());
            XPathExpression<Attribute> typeExpr = xpf.compile(XPATH_DISK_TYPE, Filters.attribute());
            XPathExpression<Attribute> fileExpr = xpf.compile(XPATH_DISK_FILE, Filters.attribute());
            XPathExpression<Attribute> devExpr = xpf.compile(XPATH_DISK_DEV, Filters.attribute());
            List<Element> disks = diskExpr.evaluate(domainXml);
            for (Element disk : disks) {
                Attribute type = typeExpr.evaluateFirst(disk);
                Attribute file = fileExpr.evaluateFirst(disk);
                Attribute dev = devExpr.evaluateFirst(disk);

                StorageVol volume = LibvirtUtil.findVolume(domain.getConnect(), file.getValue());

                ret.add(new Disk(dev.getValue(), file.getValue(), volume, type.getValue()));
            }
            return ret;
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    /**
     * Clone the domain. All disks are cloned using the original disk as backing store. The names of the disks are
     * created by suffixing the original disk name with a number.
     */
    public DomainWrapper cloneWithBackingStore(String cloneName) {
        logger.info("Creating clone from {}", getName());
        try {
            List<StorageVol> cloneDisks = Lists.newArrayList();
            int idx = 0;
            for (Disk d : getDisks()) {
                idx++;
                String clonedDisk = String.format("%s-%02d.qcow2", cloneName, idx);
                StorageVol vol = d.createCloneWithBackingStore(clonedDisk);
                logger.debug("Disk {} cloned to {}", d.getName(), clonedDisk);
                cloneDisks.add(vol);
            }

            // duplicate definition of base
            Document cloneXmlDocument = domainXml.clone();

            XPathFactory xpf = XPathFactory.instance();

            XPathExpression<Element> nameExpr = xpf.compile("/domain/name", Filters.element());
            Element nameElement = nameExpr.evaluateFirst(cloneXmlDocument);
            nameElement.setText(cloneName);

            // remove uuid so it will be generated
            cloneXmlDocument.getRootElement().removeChild("uuid");

            // keep track of who we are a clone from...
            updateCloneMetadata(cloneXmlDocument, getName(), new Date());

            XPathExpression<Element> diskExpr = xpf.compile(XPATH_DISK, Filters.element());
            XPathExpression<Attribute> fileExpr = xpf.compile(XPATH_DISK_FILE, Filters.attribute());
            List<Element> disks = diskExpr.evaluate(cloneXmlDocument);
            Iterator<StorageVol> cloneDiskIter = cloneDisks.iterator();
            for (Element disk : disks) {
                Attribute file = fileExpr.evaluateFirst(disk);
                StorageVol cloneDisk = cloneDiskIter.next();
                file.setValue(cloneDisk.getPath());
            }

            // remove mac address, so it will be generated
            XPathExpression<Element> macExpr = xpf.compile("/domain/devices/interface/mac", Filters.element());
            for (Element mac : macExpr.evaluate(cloneXmlDocument)) {
                mac.getParentElement().removeChild("mac");
            }

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
