package com.xebialabs.overcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overcast.vagrant.VagrantHelper;
import com.xebialabs.overcast.vagrant.VagrantResponse;

public class VagrantCloudHost implements CloudHost {

    private String vagrantIp;

    private VagrantHelper vagrantHelper;

    private static VagrantState initialState;

    private static Logger logger = LoggerFactory.getLogger(VagrantCloudHost.class);

    public VagrantCloudHost(String vagrantIp, VagrantHelper vagrantHelper) {
        this.vagrantIp = vagrantIp;
        this.vagrantHelper = vagrantHelper;
    }

    @Override
    public void setup() {
        VagrantResponse statusResponse = vagrantHelper.doVagrant("status");
        if (statusResponse.getReturnCode() != 0) {
            throw new RuntimeException("Cannot vagrant status host " + vagrantHelper + ": " + statusResponse.getReturnCode());
        }
        initialState = VagrantState.fromStatusString(statusResponse.getOutput());

        logger.info("Vagrant host is in state {}.", initialState.toString());
        VagrantResponse runResponse = vagrantHelper.doVagrant(VagrantState.getTransitionCommand(VagrantState.RUNNING));


        // Check for puppet errors. Not vagrant still returns 0 when puppet fails
        // May not be needed after this PR released: https://github.com/mitchellh/vagrant/pull/1175
        for (String line : runResponse.getOutput().split("\n\u001B")) {
            if (line.startsWith("[1;35merr:")) {
                logger.error("Error line in puppet output: " + line);
                throw new RuntimeException("Puppet execution contained errors. Fix them first.");
            }
        }

        if (runResponse.getReturnCode() != 0) {
            throw new RuntimeException("Cannot vagrant up host " + vagrantHelper + ": " + runResponse.getReturnCode());
        }

    }

    @Override
    public void teardown() {
        logger.info("Bringing vagrant back to {} state.", initialState.toString());
        VagrantResponse vagrantResponse = vagrantHelper.doVagrant(VagrantState.getTransitionCommand(initialState));
        if (vagrantResponse.getReturnCode() != 0) {
            throw new RuntimeException("Cannot vagrant destroy host " + vagrantHelper + ": " + vagrantResponse.getReturnCode());
        }
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
