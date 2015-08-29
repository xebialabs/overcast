/**
 *    Copyright 2012-2015 XebiaLabs B.V.
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

    private String image;
    private List<String> command;
    private boolean exposeAllPorts = false;
    private URI uri;
    private String name;
    private boolean remove;
    private boolean removeVolume;
    private List<String> env;
    private Set<String> exposedPorts;

    public DockerHost(String image, String dockerHostName) {
        this(image, dockerHostName, null);
    }

    public DockerHost(String image, String dockerHostName, Path certificatesPath) {
        try {
            this.uri = new URI(dockerHostName);
        } catch (URISyntaxException e) {
            logger.error("could not parse host name", e);
            throw new IllegalArgumentException("could not parse host name");
        }
        this.image = image;
        
        if (uri.getScheme().endsWith("https")) {
            if (certificatesPath == null) {
                throw new IllegalArgumentException("certificates are required for secured connections");
            }
            
            dockerDriver = new DockerDriver(this, certificatesPath);
        } else {
            dockerDriver = new DockerDriver(this);
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
        return uri.getHost();
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

    private static final Logger logger = LoggerFactory.getLogger(DockerHost.class);

}
