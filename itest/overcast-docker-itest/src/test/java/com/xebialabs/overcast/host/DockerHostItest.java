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

import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Ignore;
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
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DockerHostItest {
    // port range can be system specific e.g.
    // on linux cat /proc/sys/net/ipv4/ip_local_port_range
    // boot to docker had a different value
    public static final int DOCKER_PORT_RANGE_MIN = 32768;
    public static final int DOCKER_PORT_RANGE_MAX = 65535;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Ignore("relies on the default host being http://localhost:2375")
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
            assertThat(itestHost.getPort(12345), allOf(greaterThanOrEqualTo(DOCKER_PORT_RANGE_MIN), lessThanOrEqualTo(DOCKER_PORT_RANGE_MAX)));
            assertThat(itestHost.getPort(23456), allOf(greaterThanOrEqualTo(DOCKER_PORT_RANGE_MIN), lessThanOrEqualTo(DOCKER_PORT_RANGE_MAX)));
            assertThat(itestHost.getPort(34567), allOf(greaterThanOrEqualTo(DOCKER_PORT_RANGE_MIN), lessThanOrEqualTo(DOCKER_PORT_RANGE_MAX)));

            containerId = itestHost.getDockerDriver().getContainerId();
            ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);

            assertThat(containerInfo.config().image(), equalTo("busybox"));
            assertThat(containerInfo.name(), equalTo("/mycontainer"));
            assertThat(containerInfo.config().env(), hasItem("MYVAR1=AAA"));
            assertThat(containerInfo.config().env(), hasItem("MYVAR2=BBB"));
            assertThat(containerInfo.config().env(), hasItem("MYVAR3=CCC"));
            assertThat(containerInfo.config().cmd(), Matchers.equalTo((List<String>) newArrayList("/bin/sh", "-c", "while true; do echo hello world; sleep 1; done")));
            assertFalse(containerInfo.config().tty());

        } finally {
            itestHost.teardown();
        }

        thrown.expect(ContainerNotFoundException.class);
        dockerClient.inspectContainer(containerId);
    }

    @Test
    public void shouldRunAdvancedConfigWithTty() throws DockerException, InterruptedException {
        DockerHost itestHost = (DockerHost) CloudHostFactory.getCloudHost("dockerAdvancedConfigTty");
        assertThat(itestHost, notNullValue());

        DockerClient dockerClient = new DefaultDockerClient(itestHost.getUri());
        String containerId = null;

        try {
            itestHost.setup();
            containerId = itestHost.getDockerDriver().getContainerId();
            ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);

            assertTrue(containerInfo.config().tty());

        } finally {
            itestHost.teardown();
        }

        thrown.expect(ContainerNotFoundException.class);
        dockerClient.inspectContainer(containerId);
    }

    private static final Logger logger = LoggerFactory.getLogger(DockerHostItest.class);
}
