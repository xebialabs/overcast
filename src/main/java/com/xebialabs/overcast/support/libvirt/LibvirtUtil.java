package com.xebialabs.overcast.support.libvirt;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibvirtUtil {
    private static final Logger log = LoggerFactory.getLogger(LibvirtUtil.class);

    /** Look up a disk image's {@link StorageVol} in the {@link StoragePool}s attached to connection. */
    public static StorageVol findVolume(Connect connection, String path) throws LibvirtException {
        log.debug("Looking up StorageVolume for path '{}'", path);
        for (String s : connection.listStoragePools()) {
            StoragePool sp = connection.storagePoolLookupByName(s);
            for (String v : sp.listVolumes()) {
                StorageVol vol = sp.storageVolLookupByName(v);
                if (vol.getPath().equals(path)) {
                    log.debug("Found volume '{}' for path '{}'", vol.getName(), path);
                    return vol;
                }
            }
        }
        throw new LibvirtRuntimeException("no volume found for path " + path);
    }

    public static StoragePool findStoragePoolWithFile(Connect conn, String path) throws LibvirtException {
        for (String spn : conn.listStoragePools()) {
            StoragePool sp = conn.storagePoolLookupByName(spn);
            if (findVolumeInPool(sp, path) != null) {
                log.debug("Found storage pool '{}' for image '{}'", sp.getName(), path);
                return sp;
            }
        }
        throw new LibvirtRuntimeException("pool with image " + path + " not found");
    }

    private static StorageVol findVolumeInPool(StoragePool sp, String image) throws LibvirtException {
        for (String v : sp.listVolumes()) {
            StorageVol vol = sp.storageVolLookupByName(v);
            if (vol.getPath().equals(image)) {
                log.debug("Found volume '{}' for image '{}'", vol.getName(), image);
                return vol;
            }
        }
        return null;
    }
}
