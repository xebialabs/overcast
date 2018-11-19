/**
 *    Copyright 2012-2018 XebiaLabs B.V.
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
package com.xebialabs.overcast.support.libvirt.jdom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StorageVol;

import com.xebialabs.overcast.support.libvirt.Disk;
import com.xebialabs.overcast.support.libvirt.JDomUtil;
import com.xebialabs.overcast.support.libvirt.LibvirtRuntimeException;
import com.xebialabs.overcast.support.libvirt.LibvirtUtil;

public final class DiskXml {
    private static final String XPATH_DISK_DEV = "//target/@dev";
    private static final String XPATH_DISK_FILE = "//source/@file";
    private static final String XPATH_DISK_TYPE = "//driver[@name='qemu']/@type";
    private static final String XPATH_DISK = "/domain/devices/disk[@device='disk']";

    private DiskXml() {
    }

    /**
     * update the disks in the domain XML. It is assumed that the the size of the volumes is the same as the number of
     * disk elements and that the order is the same.
     */
    public static void updateDisks(Document domainXml, List<StorageVol> volumes) throws LibvirtException {
        XPathFactory xpf = XPathFactory.instance();
        XPathExpression<Element> diskExpr = xpf.compile(XPATH_DISK, Filters.element());
        XPathExpression<Attribute> fileExpr = xpf.compile(XPATH_DISK_FILE, Filters.attribute());
        List<Element> disks = diskExpr.evaluate(domainXml);
        Iterator<StorageVol> cloneDiskIter = volumes.iterator();
        for (Element disk : disks) {
            Attribute file = fileExpr.evaluateFirst(disk);
            StorageVol cloneDisk = cloneDiskIter.next();
            file.setValue(cloneDisk.getPath());
        }
    }

    /** Get the disks connected to the domain. */
    public static List<Disk> getDisks(Connect connect, Document domainXml) {
        try {
            List<Disk> ret = new ArrayList<>();
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

                StorageVol volume = LibvirtUtil.findVolume(connect, file.getValue());

                ret.add(new Disk(dev.getValue(), file.getValue(), volume, type.getValue()));
            }
            return ret;
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    public static String cloneVolumeXml(Disk backingDisk, String clonedDiskName) throws IOException {
        Element volume = new Element("volume");
        volume.addContent(new Element("name").setText(clonedDiskName));
        volume.addContent(new Element("allocation").setText("0"));
        volume.addContent(new Element("capacity").setText("" + backingDisk.getInfo().capacity));
        Element target = new Element("target");
        volume.addContent(target);
        target.addContent(new Element("format").setAttribute("type", backingDisk.format));
        target.addContent(new Element("compat").setText("1.1"));
        Element backingStore = new Element("backingStore");
        volume.addContent(backingStore);
        backingStore.addContent(new Element("path").setText(backingDisk.file));
        backingStore.addContent(new Element("format").setAttribute("type", backingDisk.format));

        return JDomUtil.elementToString(volume);
    }
}
