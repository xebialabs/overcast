package com.xebialabs.overcast.host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overcast.command.Command;
import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.command.NonZeroCodeException;
import com.xebialabs.overcast.support.vagrant.VagrantDriver;
import com.xebialabs.overcast.support.vagrant.VagrantState;
import com.xebialabs.overcast.support.virtualbox.VirtualboxDriver;

class CachedVagrantCloudHost extends VagrantCloudHost {

    public final static String EXPIRATION_TAG_PROPERTY_KEY = "overcastExpirationTag";

    private Command expirationCmd;

    private VirtualboxDriver virtualboxDriver;

    private CommandProcessor commandProcessor;

    private static Logger logger = LoggerFactory.getLogger(VagrantCloudHost.class);

    public CachedVagrantCloudHost(String vagrantVm, String vagrantIp, Command expirationCmd, VagrantDriver vagrantDriver, VirtualboxDriver virtualboxDriver, CommandProcessor commandProcessor) {
        super(vagrantVm, vagrantIp, vagrantDriver);
        this.virtualboxDriver = virtualboxDriver;
        this.expirationCmd = expirationCmd;
        this.commandProcessor = commandProcessor;
    }


    @Override
    public void setup() {

        String expirationTag;

        try {
            expirationTag = commandProcessor.run(expirationCmd).getOutput();
        } catch (NonZeroCodeException e) {
            throw toExternalException(e);
        }

        super.setup();
        virtualboxDriver.setExtraData(vagrantVm, EXPIRATION_TAG_PROPERTY_KEY, expirationTag);

    }

    @Override
    public void teardown() {
        String tag = virtualboxDriver.getExtraData(vagrantVm, EXPIRATION_TAG_PROPERTY_KEY);
        if (tag == null) {
            super.teardown();
        } else {
            virtualboxDriver.powerOff(vagrantVm);
        }
    }
    @Override
    public String getHostName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getPort(final int port) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // Helper methods

    private IllegalArgumentException toExternalException(final NonZeroCodeException e) {
        return new IllegalArgumentException(
                String.format(
                        "Command %s returned code %s with the following errors: \n\n%s\n",
                        e.getCommand().toString(),
                        e.getResponse().getReturnCode(),
                        e.getResponse().getErrors() + "\n\n" + e.getResponse().getOutput()
                )
        );
    }
}
