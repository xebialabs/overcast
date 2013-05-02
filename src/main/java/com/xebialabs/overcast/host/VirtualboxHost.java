package com.xebialabs.overcast.host;

import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.support.virtualbox.VirtualboxDriver;

public class VirtualboxHost implements CloudHost {

    private String ip, uuid, snapshot;

    public VirtualboxHost(final String ip, final String uuid, final String snapshot) {
        this.ip = ip;
        this.uuid = uuid;
        this.snapshot = snapshot;
    }

    @Override
    public void setup() {
        new VirtualboxDriver(CommandProcessor.atCurrentDir()).loadSnapshot(uuid, snapshot);
    }

    @Override
    public void teardown() {
        new VirtualboxDriver(CommandProcessor.atCurrentDir()).powerOff(uuid);
    }

    @Override
    public String getHostName() {
        return ip;
    }

    @Override
    public int getPort(final int port) {
        return port;
    }
}
