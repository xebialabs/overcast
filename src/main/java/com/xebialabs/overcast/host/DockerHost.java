package com.xebialabs.overcast.host;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overcast.support.docker.DockerDriver;

public class DockerHost implements CloudHost {

    private final DockerDriver dockerDriver;

    private String image;
    private List<String> command;
    private boolean exposeAllPorts = false;
    private URI uri;

    public DockerHost(final String uri, final String image, final List<String> command, final boolean exposeAllPorts) {
        this.image = image;
        this.command = command;
        this.exposeAllPorts = exposeAllPorts;

        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            logger.error("could not parse host name", e);
            throw new IllegalArgumentException("could not parse host name");
        }

        dockerDriver = new DockerDriver(this);

    }

    @Override
    public void setup() {
        dockerDriver.pullImage();
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

    private static final Logger logger = LoggerFactory.getLogger(DockerHost.class);

}
