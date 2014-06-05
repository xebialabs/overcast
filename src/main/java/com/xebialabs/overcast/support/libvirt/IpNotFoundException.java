package com.xebialabs.overcast.support.libvirt;

/** Indicates an {@link IpLookupStrategy} could not find the IP for a certain MAC. */
@SuppressWarnings("serial")
public class IpNotFoundException extends RuntimeException {
    public IpNotFoundException(String message) {
        super(message);
    }
}
