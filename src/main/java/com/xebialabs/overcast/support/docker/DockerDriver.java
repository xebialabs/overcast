/**
 *    Copyright 2012-2016 XebiaLabs B.V.
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
package com.xebialabs.overcast.support.docker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ImageNotFoundException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.xebialabs.overcast.host.DockerHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerDriver {

    private DockerHost dockerHost;

    private Map portMappings;
    private DockerClient dockerClient;
    private String containerId;
    private ContainerConfig config;

    public DockerDriver(final DockerHost dockerHost) {
        this(dockerHost, null);
    }

    public DockerDriver(final DockerHost dockerHost, final Path certificatesPath) {
        this.dockerHost = dockerHost;

        if (certificatesPath != null) {
            try {
                dockerClient = new DefaultDockerClient(dockerHost.getUri(), new DockerCertificates(certificatesPath));
            } catch (final DockerCertificateException e) {
                logger.error("could not read certificates", e);
                throw new IllegalArgumentException("could not read certificates");
            }
        } else {
            dockerClient = new DefaultDockerClient(dockerHost.getUri());
        }
    }

    private void buildImageConfig() {
        final ContainerConfig.Builder configBuilder = ContainerConfig.builder().image(dockerHost.getImage());
        if (dockerHost.getCommand() != null){
            configBuilder.cmd(dockerHost.getCommand());
        }
        if (dockerHost.getEnv() != null){
            configBuilder.env(dockerHost.getEnv());
        }
        if (dockerHost.getExposedPorts() != null) {
            configBuilder.exposedPorts(dockerHost.getExposedPorts());
        }
        if (dockerHost.isTty()) {
            configBuilder.tty(true);
        }

        final HostConfig.Builder hostConfigBuilder = HostConfig.builder()
                .publishAllPorts(dockerHost.isExposeAllPorts())
                .links(dockerHost.getLinks());
        
        if (dockerHost.hasPortBindings()) {
            final Map<String, List<PortBinding>> portBindings = new HashMap<>();
            for (String binding : dockerHost.getPortBindings()) {
                final String[] bindings = binding.split(":");
                final String containerPort = bindings[1];
                final PortBinding hostBinding = PortBinding.of("0.0.0.0", bindings[0]);
                portBindings.put(containerPort, Collections.singletonList(hostBinding));
            }
            hostConfigBuilder.portBindings(portBindings);
        }
        
        configBuilder.hostConfig(hostConfigBuilder.build());
        config = configBuilder.build();
    }

    public void runContainer() {
        buildImageConfig();

        try {
            try {
                createImage();
            } catch(ImageNotFoundException e){
                dockerClient.pull(dockerHost.getImage(), new ProcessHandlerLogger());
                createImage();
            }

            dockerClient.startContainer(containerId);

            final ContainerInfo info = dockerClient.inspectContainer(containerId);
            portMappings = info.networkSettings().ports();

        } catch (Exception e) {
            logger.error("Error while setting up docker host: ", e);
        }

    }

    private void createImage() throws DockerException, InterruptedException {
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
                dockerClient.removeContainer(containerId, dockerHost.isRemoveVolume());
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
