package com.xebialabs.overcast.host;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overcast.support.docker.DockerDriver;

public class DockerHost implements CloudHost {

    private final DockerDriver dockerDriver;

    private String image;
    private String buildPath;
    private String certPath;
    private List<String> command;
    private boolean exposeAllPorts = false;
    private URI uri;
    private String name;
    private boolean remove;
    private List<String> env;
    private Set<String> exposedPorts;

    public DockerHost(String image, String dockerHostName) {
        try {
            this.uri = new URI(dockerHostName);
        } catch (URISyntaxException e) {
            logger.error("could not parse host name", e);
            throw new IllegalArgumentException("could not parse host name");
        }
        this.image = image;
        dockerDriver = new DockerDriver(this);
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

    public String getBuildPath() {
        return buildPath;
    }

    public void setBuildPath(final String buildPath) {
        this.buildPath = buildPath;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(final String certPath) {
        this.certPath = certPath;
    }

    private static final Logger logger = LoggerFactory.getLogger(DockerHost.class);

}
