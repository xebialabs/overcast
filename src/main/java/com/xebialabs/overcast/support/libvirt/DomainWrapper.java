package com.xebialabs.overcast.support.libvirt;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.libvirt.StorageVol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.StringInputStream;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DomainWrapper {
    private static final String XPATH_DISK_DEV = "//target/@dev";
    private static final String XPATH_DISK_FILE = "//source/@file";
    private static final String XPATH_DISK_TYPE = "//driver[@name='qemu']/@type";
    private static final String XPATH_DISK = "/domain/devices/disk[@device='disk']";
    private static final Logger log = LoggerFactory.getLogger(DomainWrapper.class);
    private Document domainXml;
    private Domain domain;

    private DomainWrapper(Domain domain, Document domainXml) {
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

    public static DomainWrapper newWrapper(Domain domain) {
        try {
            SAXBuilder sax = new SAXBuilder();

            Document dx = sax.build(new StringInputStream(domain.getXMLDesc(0)));
            return new DomainWrapper(domain, dx);
        } catch (JDOMException e) {
            throw new LibvirtRuntimeException(e);
        } catch (IOException e) {
            throw new LibvirtRuntimeException("Unable to create DomainWrapper", e);
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException("Unable to create DomainWrapper", e);
        }
    }

    public void destroyWithDisks() {
        try {
            List<Disk> disks = getDisks();
            log.info("Undefining domain {}", domain.getName());
            domain.undefine();
            log.info("Destroying domain {}", domain.getName());
            domain.destroy();

            // this will not destroy the backing store disks.
            for (Disk d : disks) {
                log.info("Removing disk {}", d.getName());
                d.getVolume().delete(0);
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
            log.debug("Detecting network of type '{}'", interfaceType);
            if ("bridge".equals(interfaceType)) {
                Element macElement = iface.getChild("mac");
                String mac = macElement.getAttribute("address").getValue();
                Element sourceElement = iface.getChild("source");
                String bridge = sourceElement.getAttribute("bridge").getValue();
                log.info("Detected '{}' bridged '{}' mac '{}'", interfaceType, bridge, mac);
                macs.put(bridge, mac);
            } else if ("network".equals(interfaceType)) {
                log.warn("Ignoring network of type {}", interfaceType);
            } else {
                log.warn("Ignoring network of type {}", interfaceType);
            }
        }
        return macs;
    }

    public String getMac(String id) {
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

    /** Clone the domain. All disks are cloned using the original disk as backing store. */
    public DomainWrapper cloneWithBackingStore(String cloneName) {
        log.info("Creating clone from {}", getName());
        try {
            List<StorageVol> cloneDisks = Lists.newArrayList();
            for (Disk d : getDisks()) {
                String clonedDisk = String.format("%s-%s.qcow2", d.getBaseName(), cloneName);
                StorageVol vol = d.createCloneWithBackingStore(clonedDisk);
                log.debug("Disk {} cloned to {}", d.getName(), clonedDisk);
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

            StringWriter vsw = new StringWriter();
            XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
            xout.output(cloneXmlDocument, vsw);
            String cloneXml = vsw.toString();
            log.debug("Clone xml={}", cloneXml);

            // Domain cloneDomain = domain.getConnect().domainCreateXML(cloneXml, 0);
            Domain cloneDomain = domain.getConnect().domainDefineXML(cloneXml);
            String createdCloneXml = cloneDomain.getXMLDesc(0);
            log.debug("Created clone xml: {}", createdCloneXml);
            cloneDomain.create();
            log.debug("Starting clone: '{}'", cloneDomain.getName());

            DomainWrapper clone = newWrapper(cloneDomain);
            return clone;
        } catch (IOException e) {
            throw new LibvirtRuntimeException("Unable to clone domain", e);
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException("Unable to clone domain", e);
        }
    }
}
