package com.xebialabs.overcast.support.libvirt;

public interface IpLookupStrategy {
    String lookup(String mac);
}
