package com.xebialabs.overcast.host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overcast.command.CommandResponse;
import com.xebialabs.overcast.support.vagrant.VagrantDriver;
import com.xebialabs.overcast.support.vagrant.VagrantState;

import static com.xebialabs.overcast.support.vagrant.VagrantState.getTransitionCommand;

public class VagrantCloudHost implements CloudHost {

    private String vagrantIp;

    private String vagrantVm;

    private VagrantDriver vagrantDriver;

    private static VagrantState initialState;

    private static Logger logger = LoggerFactory.getLogger(VagrantCloudHost.class);

    public VagrantCloudHost(String vagrantVm, String vagrantIp, VagrantDriver vagrantDriver) {
        this.vagrantIp = vagrantIp;
        this.vagrantDriver = vagrantDriver;
        this.vagrantVm = vagrantVm;
    }

    @Override
    public void setup() {
        CommandResponse statusResponse = vagrantDriver.status(vagrantVm);
        initialState = VagrantState.fromStatusString(statusResponse.getOutput());
        logger.info("Vagrant host is in state {}.", initialState.toString());
        vagrantDriver.doVagrant(vagrantVm, getTransitionCommand(VagrantState.RUNNING));
    }

    @Override
    public void teardown() {
        logger.info("Bringing vagrant back to {} state.", initialState.toString());
        vagrantDriver.doVagrant(vagrantVm, getTransitionCommand(initialState));
    }

    @Override
    public String getHostName() {
        return vagrantIp;
    }

    @Override
    public int getPort(int port) {
        return port;
    }

}
