package com.xebialabs.overcast.host;

import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.libvirt.DomainInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerInfo;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class DockerHostTest {


    @Test
    public void shouldBootMongoDockerHost() throws DockerException, InterruptedException {
        DockerHost itestHost = (DockerHost) getCloudHost("dockerHostMongo");
        assertThat(itestHost, notNullValue());

        try {
            itestHost.setup();
            assertThat(itestHost.getHostName(), equalTo("localhost"));
            assertThat(itestHost.getPort(27017), greaterThan(0));

            DockerClient dockerClient = new DefaultDockerClient(itestHost.getUri());
            ContainerInfo containerInfo = dockerClient.inspectContainer(itestHost.getDockerDriver().getContainerId());
            assertThat(containerInfo.config().image(), equalTo("mongo"));
            assertThat(containerInfo.networkSettings().ports(), not(nullValue()));
            assertThat(containerInfo.networkSettings().ports().size(), equalTo(1));
        } finally {
            itestHost.teardown();
        }
    }

    @Test
    public void shouldBootUbuntuDockerHost() throws DockerException, InterruptedException {
        DockerHost itestHost = (DockerHost) getCloudHost("dockerHostUbuntu");
        assertThat(itestHost, notNullValue());

        try {
            itestHost.setup();
            assertThat(itestHost.getHostName(), equalTo("localhost"));

            DockerClient dockerClient = new DefaultDockerClient(itestHost.getUri());
            ContainerInfo containerInfo = dockerClient.inspectContainer(itestHost.getDockerDriver().getContainerId());
            assertThat(containerInfo.config().image(), equalTo("ubuntu"));
            assertThat(containerInfo.config().cmd(), Matchers.equalTo((List<String>)newArrayList("sh", "-c", "while :; do echo hello; done")));
        } finally {
            itestHost.teardown();
        }
    }

    public CloudHost getCloudHost(String name) {
        try {
            return CloudHostFactory.getCloudHost(name);
        } catch (IllegalStateException e) {
            logger.warn("Host not found", e);
            return null;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DockerHostTest.class);

}
