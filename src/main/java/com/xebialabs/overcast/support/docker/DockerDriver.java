package com.xebialabs.overcast.support.docker;

import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.*;

import com.xebialabs.overcast.host.DockerHost;

public class DockerDriver {

    private DockerHost dockerHost;

    private Map portMappings;
    private DockerClient dockerClient;
    private String containerId;
    private ContainerConfig config;

    public DockerDriver(final DockerHost dockerHost) {
        this.dockerHost = dockerHost;
        dockerClient = new DefaultDockerClient(dockerHost.getUri());

        buildImageConfig();
    }

    private void buildImageConfig() {
        final ContainerConfig.Builder configBuilder = ContainerConfig.builder().image(dockerHost.getImage());
        if(dockerHost.getCommand() != null){
            configBuilder.cmd(dockerHost.getCommand());
        }
        config = configBuilder.build();
    }


    public void pullImage(){
        try {
            dockerClient.pull(dockerHost.getImage(), new ProcessHandlerLogger());
        } catch (Exception e) {
            logger.error("Error while pulling container: ", e);
        }
    }

    public void runContainer() {
        try {
            containerId = dockerClient.createContainer(config).id();

            if(dockerHost.isExposeAllPorts()) {
                HostConfig hostConfig = HostConfig.builder().publishAllPorts(true).build();
                dockerClient.startContainer(containerId, hostConfig);
            } else {
                dockerClient.startContainer(containerId);
            }

            final ContainerInfo info = dockerClient.inspectContainer(containerId);
            portMappings = info.networkSettings().ports();

        } catch (Exception e) {
            logger.error("Error while setting up docker host: ", e);
        }

    }


    public void killAndRemoveContainer() {
        try {
            dockerClient.killContainer(containerId);
            dockerClient.removeContainer(containerId);
        } catch (Exception e) {
            logger.error("Error while tearing down docker host: ", e);
        }
    }

    public int getPort(int port) {
        ArrayList<PortBinding> bindings = (ArrayList<PortBinding>) portMappings.get(port+"/tcp");
        if(bindings != null) {
            PortBinding portBinding = bindings.get(0);
            if (portBinding != null) {
                return Integer.parseInt(portBinding.hostPort());
            } else {
                throw new IllegalArgumentException("Port not available");
            }
        } else {
            throw new IllegalArgumentException("Port not available");
        }

    }

    public String getContainerId() {
        return containerId;
    }

    private static final Logger logger = LoggerFactory.getLogger(DockerDriver.class);
}
