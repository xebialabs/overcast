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
package com.xebialabs.overcast.host;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overcast.support.docker.DockerDriver;

public class DockerHost implements CloudHost {

    private final DockerDriver dockerDriver;
    private final String image;
    private final URI uri; // allowed to be null when configuring via DOCKER_* environment variables

    private String name;
    private List<String> env;
    private List<String> command;
    private boolean tty;

    private boolean remove;
    private boolean removeVolume;

    private boolean exposeAllPorts = false;
    private Set<String> exposedPorts;

    private List<String> links;

    public DockerHost(String image, String dockerHostName, Path certificatesPath) {
        try {
            this.image = image;

            if (dockerHostName != null) {
                this.uri = new URI(dockerHostName);

                if ("https".equals(uri.getScheme())) {
                    if (certificatesPath == null) {
                        throw new IllegalArgumentException("<host>.certificates must be configured for https connection");
                    }
                }
            } else {
                this.uri = null;
            }

            dockerDriver = new DockerDriver(this, certificatesPath);
        } catch (URISyntaxException e) {
            logger.error("Invalid dockerHost " + dockerHostName, e);
            throw new IllegalArgumentException("Invalid dockerHost " + e.getMessage());
        }
    }

    @Override
    public void setup() {
        dockerDriver.runContainer();
    }

    @Override
    public void teardown() {
        dockerDriver.killAndRemoveContainer();
    }

    @Override
    public int getPort(final int port) {
        return dockerDriver.getPort(port);
    }

    @Override
    public String getHostName() {
        return dockerDriver.getHost();
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public List<String> getCommand() {
        return command;
    }

    public boolean isExposeAllPorts() {
        return exposeAllPorts;
    }

    public URI getUri() {
        return uri;
    }

    public DockerDriver getDockerDriver() {
        return dockerDriver;
    }

    public void setCommand(final List<String> command) {
        this.command = command;
    }

    public void setExposeAllPorts(final boolean exposeAllPorts) {
        this.exposeAllPorts = exposeAllPorts;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(final boolean remove) {
        this.remove = remove;
    }

    public boolean isRemoveVolume() {
        return removeVolume;
    }

    public void setRemoveVolume(final boolean removeVolume) {
        this.removeVolume = removeVolume;
    }

    public List<String> getEnv() {
        return env;
    }

    public void setEnv(final List<String> env) {
        this.env = env;
    }

    public Set<String> getExposedPorts() {
        return exposedPorts;
    }

    public void setExposedPorts(final Set<String> exposedPorts) {
        this.exposedPorts = exposedPorts;
    }

    public boolean isTty() {
        return tty;
    }

    public void setTty(boolean tty) {
        this.tty = tty;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(final List<String> links) {
        this.links = links;
    }

    private static final Logger logger = LoggerFactory.getLogger(DockerHost.class);
}
