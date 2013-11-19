package com.xebialabs.overcast.support.libvirt;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.libvirt.StorageVolInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

public class Disk {
    private static final Logger log = LoggerFactory.getLogger(Disk.class);

    public String device;
    public String file;
    public String format;
    private StorageVol volume;

    public Disk(String device, String file, StorageVol volume, String format) {
        checkNotNull(emptyToNull(device));
        checkNotNull(emptyToNull(file));
        checkNotNull(volume);
        checkNotNull(emptyToNull(format));

        this.device = device;
        this.file = file;
        this.volume = volume;
        this.format = format;
    }

    public StorageVolInfo getInfo() {
        try {
            return volume.getInfo();
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    public String getName() {
        try {
            return volume.getName();
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    public String getBaseName() {
        String name = getName();
        int idx = name.lastIndexOf('.');
        if (idx == -1) {
            return name;
        }
        return name.substring(0, idx);
    }

    public StorageVol getVolume() {
        return volume;
    }

    public StoragePool getStoragePool() {
        try {
            return volume.storagePoolLookupByVolume();
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    public StorageVol createCloneWithBackingStore(String name) {
        try {
            Element volume = new Element("volume");
            volume.addContent(new Element("name").setText(name));
            volume.addContent(new Element("allocation").setText("0"));
            volume.addContent(new Element("capacity").setText("" + getInfo().capacity));
            Element target = new Element("target");
            volume.addContent(target);
            target.addContent(new Element("format").setAttribute("type", format));
            target.addContent(new Element("compat").setText("1.1"));
            Element backingStore = new Element("backingStore");
            volume.addContent(backingStore);
            backingStore.addContent(new Element("path").setText(file));
            backingStore.addContent(new Element("format").setAttribute("type", format));

            StringWriter vsw = new StringWriter();
            XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
            xout.output(volume, vsw);
            String volumeXml = vsw.toString();
            log.debug("Creating volume with xml={}", volumeXml);
            StorageVol vol = getStoragePool().storageVolCreateXML(volumeXml, 0);
            return vol;
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        } catch (IOException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("name", getName())
            .add("format", format)
            .add("device", device)
            .add("file", file).toString();
    }
}
