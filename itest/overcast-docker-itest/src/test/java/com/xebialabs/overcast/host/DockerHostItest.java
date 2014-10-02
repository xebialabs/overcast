package com.xebialabs.overcast.host;

import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spotify.docker.client.ContainerNotFoundException;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerInfo;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class DockerHostItest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    // docker port range 49153 to 65535

    @Test
    public void shouldRunMinimalConfig() throws DockerException, InterruptedException {
        DockerHost itestHost = (DockerHost) CloudHostFactory.getCloudHost("dockerMinimalConfig");
        assertThat(itestHost, notNullValue());

        DockerClient dockerClient = new DefaultDockerClient(itestHost.getUri());
        String containerId = null;

        try {
            itestHost.setup();
            assertThat(itestHost.getHostName(), equalTo("localhost"));

            dockerClient = new DefaultDockerClient(itestHost.getUri());
            containerId = itestHost.getDockerDriver().getContainerId();
            dockerClient.inspectContainer(containerId);
        } finally {
            itestHost.teardown();
        }

        thrown.expect(ContainerNotFoundException.class);
        dockerClient.inspectContainer(containerId);
    }

    @Test
    public void shouldRunAdvancedConfig() throws DockerException, InterruptedException {


        DockerHost itestHost = (DockerHost) CloudHostFactory.getCloudHost("dockerAdvancedConfig");
        assertThat(itestHost, notNullValue());

        DockerClient dockerClient = new DefaultDockerClient(itestHost.getUri());
        String containerId = null;

        try {
            itestHost.setup();
            assertThat(itestHost.getHostName(), equalTo("localhost"));
            assertThat(itestHost.getPort(12345), greaterThanOrEqualTo(49153));
            assertThat(itestHost.getPort(12345), lessThanOrEqualTo(65535));
            assertThat(itestHost.getPort(23456), greaterThanOrEqualTo(49153));
            assertThat(itestHost.getPort(23456), lessThanOrEqualTo(65535));
            assertThat(itestHost.getPort(34567), greaterThanOrEqualTo(49153));
            assertThat(itestHost.getPort(34567), lessThanOrEqualTo(65535));

            containerId = itestHost.getDockerDriver().getContainerId();
            ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);

            assertThat(containerInfo.config().image(), equalTo("busybox"));
            assertThat(containerInfo.name(), equalTo("/mycontainer"));
            assertThat(containerInfo.config().env(), hasItem("MYVAR1=AAA"));
            assertThat(containerInfo.config().env(), hasItem("MYVAR2=BBB"));
            assertThat(containerInfo.config().env(), hasItem("MYVAR3=CCC"));
            assertThat(containerInfo.config().cmd(), Matchers.equalTo((List<String>) newArrayList("/bin/sh", "-c", "while true; do echo hello world; sleep 1; done")));

        } finally {
            itestHost.teardown();
        }

        thrown.expect(ContainerNotFoundException.class);
        dockerClient.inspectContainer(containerId);
    }

    private static final Logger logger = LoggerFactory.getLogger(DockerHostItest.class);

}
