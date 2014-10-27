package com.xebialabs.overcast.support.docker;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spotify.docker.client.*;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

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

        try {
            DefaultDockerClient.Builder builder = new DefaultDockerClient.Builder();
            builder.uri(dockerHost.getUri());
            if (dockerHost.getCertPath() != null) {
                builder.dockerCertificates(new DockerCertificates(Paths.get(dockerHost.getCertPath())));
            }
            dockerClient = builder.build();
        } catch(DockerCertificateException e){
            throw new RuntimeException("Error while setting up certificates", e);
        }
    }

    private void buildImageConfig() {
        final ContainerConfig.Builder configBuilder = ContainerConfig.builder().image(dockerHost.getImage());
        if(dockerHost.getCommand() != null){
            configBuilder.cmd(dockerHost.getCommand());
        }
        if(dockerHost.getEnv() != null){
            configBuilder.env(dockerHost.getEnv());
        }
        if(dockerHost.getExposedPorts() != null) {
            configBuilder.exposedPorts(dockerHost.getExposedPorts());
        }

        config = configBuilder.build();
    }

    public void runContainer() {

        if(dockerHost.getBuildPath() != null){
            buildImage();
        }

        buildImageConfig();

        try {
            try {
                createContainer();
            } catch(ImageNotFoundException e){
                dockerClient.pull(dockerHost.getImage(), new ProcessHandlerLogger());
                createContainer();
            }

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

    private void buildImage() {
        try {
            URI uri = this.getClass().getResource(String.format("/%s", dockerHost.getBuildPath())).toURI();
            dockerClient.build(Paths.get(uri), dockerHost.getImage() );
        } catch (Exception e) {
            logger.error("Error while building docker image:", e);
        }
    }

    private void createContainer() throws DockerException, InterruptedException {
        if(dockerHost.getName() == null){
            containerId = dockerClient.createContainer(config).id();
        } else {
            containerId = dockerClient.createContainer(config, dockerHost.getName()).id();
        }
    }


    public void killAndRemoveContainer() {
        try {
            dockerClient.killContainer(containerId);
            if(dockerHost.isRemove()) {
                dockerClient.removeContainer(containerId);
            }
        } catch (Exception e) {
            logger.error("Error while tearing down docker host: ", e);
        }
    }

    public int getPort(int port) {
        ArrayList<PortBinding> bindings = (ArrayList<PortBinding>) portMappings.get(port+"/tcp");
        if(bindings != null && bindings.size() > 0) {
            return Integer.parseInt(bindings.get(0).hostPort());
        } else {
            throw new IllegalArgumentException("Port not available");
        }

    }

    public String getContainerId() {
        return containerId;
    }

    private static final Logger logger = LoggerFactory.getLogger(DockerDriver.class);
}
