package com.xebialabs.overcast.support.libvirt;

public interface IpLookupStrategy {
    /**
     * attempt to lookup an IP for a MAC. May throw {@link IpLookupException} when the lookup fails abnormally. May
     * throw {@link IpNotFoundException} when the IP was not found.
     */
    String lookup(String mac);
}
