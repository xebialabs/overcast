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
            logger.info("Executing expiration command: {}", expirationCmd);
            expirationTag = commandProcessor.run(expirationCmd).getOutput().trim();
        } catch (NonZeroCodeException e) {
            throw toExternalException(e);
        }

        logger.info("Expiration tag: {}", expirationTag);

        if (VagrantState.NOT_CREATED.equals(vagrantDriver.state(vagrantVm))) {
            // Cache empty
            super.setup();
            logger.info("Attaching tag to the VM");
            virtualboxDriver.setExtraData(vagrantVm, EXPIRATION_TAG_PROPERTY_KEY, expirationTag);
            logger.info("Taking a snapshot to be used in future when the tag matches");
            virtualboxDriver.createSnapshot(vagrantVm, expirationTag);
            return;
        }

        if (expirationTag.equals(virtualboxDriver.getExtraData(vagrantVm, EXPIRATION_TAG_PROPERTY_KEY))) {
            logger.info("Cache hit. Loading the latest snapshot of the VM");
            logger.info("Waiting for the VM to become accessible via SSH");
            virtualboxDriver.loadLatestSnapshot(vagrantVm);
            vagrantDriver.doVagrant(vagrantVm, "ssh", "-c", "'hostname'");
        } else {
            logger.info("Expiration tag does not match. Recreating the VM");
            vagrantDriver.doVagrant(vagrantVm, "destroy", "-f");
            this.setup();
        }
    }

    @Override
    public void teardown() {
        logger.info("Preparing to teardown the VM");
        String tag = virtualboxDriver.getExtraData(vagrantVm, EXPIRATION_TAG_PROPERTY_KEY);
        if (tag == null) {
            logger.debug("Not found any expiration tag. Falling back to the standard process.");
            super.teardown();
        } else {
            logger.info("Found expiration tag {}", tag);
            logger.info("Powering off the VM");
            virtualboxDriver.powerOff(vagrantVm);
        }
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
