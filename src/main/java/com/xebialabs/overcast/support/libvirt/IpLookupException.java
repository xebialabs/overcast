package com.xebialabs.overcast.support.libvirt;

/** Indicates a failure occurred while an {@link IpLookupStrategy} was trying to look up the IP on a host. */
@SuppressWarnings("serial")
public class IpLookupException extends RuntimeException {
    public IpLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
