package com.xebialabs.overcast.support.libvirt;

import static com.xebialabs.overcast.OvercastProperties.getRequiredOvercastProperty;

/**
 * Simple {@link IpLookupStrategy} that returns a pre-configured static IP.
 */
public class StaticIpLookupStrategy implements IpLookupStrategy {
    private static final String STATIC_IP_SUFFIX = ".static.ip";

    private String ip;

    public StaticIpLookupStrategy(String ip) {
        this.ip = ip;
    }

    public static StaticIpLookupStrategy create(String prefix) {
        String ip = getRequiredOvercastProperty(prefix + STATIC_IP_SUFFIX);
        return new StaticIpLookupStrategy(ip);
    }

    @Override
    public String lookup(String mac) {
        return ip;
    }
}
