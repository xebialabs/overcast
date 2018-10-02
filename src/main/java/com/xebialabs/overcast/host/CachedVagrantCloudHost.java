/**
 *    Copyright 2012-2018 XebiaLabs B.V.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xebialabs.overcast.host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import com.xebialabs.overcast.command.Command;
import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.command.NonZeroCodeException;
import com.xebialabs.overcast.support.vagrant.VagrantDriver;
import com.xebialabs.overcast.support.vagrant.VagrantState;
import com.xebialabs.overcast.support.virtualbox.VirtualboxDriver;
import com.xebialabs.overcast.support.virtualbox.VirtualboxState;
import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;

class CachedVagrantCloudHost extends VagrantCloudHost {

    public final static String EXPIRATION_TAG_PROPERTY_KEY = "overcastExpirationTag";
    public static final int CONNECTION_ATTEMPTS = 100;
    public static final int CONNECTION_RETRY_DELAY = 2000;

    private Command expirationCmd;

    private VirtualboxDriver virtualboxDriver;

    private CommandProcessor commandProcessor;

    private OverthereConnectionBuilder connectionBuilder;

    private static Logger logger = LoggerFactory.getLogger(VagrantCloudHost.class);

    public CachedVagrantCloudHost(String vm, String ip, Command cmd, VagrantDriver vagrantDriver, VirtualboxDriver vboxDriver, CommandProcessor commandProcessor, final OverthereConnectionBuilder cb) {
        super(vm, ip, vagrantDriver);
        this.virtualboxDriver = vboxDriver;
        this.expirationCmd = cmd;
        this.commandProcessor = commandProcessor;
        this.connectionBuilder = cb;
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
            virtualboxDriver.loadLatestSnapshot(vagrantVm);
            logger.info("Waiting for the VM to become accessible...");

            boolean connected = false;
            int currentAttempt = 1;

            while(!connected && currentAttempt < CONNECTION_ATTEMPTS) {
                try {
                    OverthereConnection c = connectionBuilder.connect();
                    try {
                        c.execute(CmdLine.build("hostname"));
                        connected = true;
                    } finally {
                        c.close();
                    }
                } catch (RuntimeIOException re) {
                    currentAttempt++;
                    logger.info(re.getMessage());
                    logger.info("Proceeding with attempt {}", currentAttempt);
                    try {
                        Thread.sleep(CONNECTION_RETRY_DELAY);
                    } catch (InterruptedException se) {
                        Throwables.propagate(se);
                    }
                }
            }

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
            VirtualboxState state = virtualboxDriver.vmState(vagrantVm);
            if (VirtualboxState.RUNNING == state) {
                logger.info("Powering off VM '{}'", vagrantVm);
                virtualboxDriver.powerOff(vagrantVm);
            } else {
                logger.info("VM '{}' already shut down (state={}).", vagrantVm, state);
            }
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
