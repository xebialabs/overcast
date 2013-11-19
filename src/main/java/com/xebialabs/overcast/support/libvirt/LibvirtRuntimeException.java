package com.xebialabs.overcast.support.libvirt;

@SuppressWarnings("serial")
public class LibvirtRuntimeException extends RuntimeException {
    public LibvirtRuntimeException(Throwable e) {
        super(e);
    }

    public LibvirtRuntimeException(String message, Throwable e) {
        super(message, e);
    }

    public LibvirtRuntimeException(String message) {
        super(message);
    }
}
