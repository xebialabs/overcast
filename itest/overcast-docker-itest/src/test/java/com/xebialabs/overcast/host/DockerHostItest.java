/**
 *    Copyright 2012-2021 Digital.ai
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

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerInfo;
import com.xebialabs.overcast.support.docker.Config;
import com.xebialabs.overcast.support.docker.DockerDriver;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.xebialabs.overcast.OvercastProperties.getOvercastProperty;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DockerHostItest {
    // port range can be system specific e.g.
    // on linux cat /proc/sys/net/ipv4/ip_local_port_range
    // boot to docker had a different value
    public static final int DOCKER_PORT_RANGE_MIN = 32768;
    public static final int DOCKER_PORT_RANGE_MAX = 65535;

    // configuration keys
    public static final String DOCKER_ADVANCED_CONFIG = "dockerAdvancedConfig";
    public static final String DOCKER_MINIMAL_CONFIG = "dockerMinimalConfig";
    public static final String DOCKER_ADVANCED_CONFIG_TTY = "dockerAdvancedConfigTty";
    public static final String DOCKER_LINKS_CONFIG = "dockerLinksConfig";

    public DockerClient createDockerClient(String label) {
        try {
            String hostProperty = getOvercastProperty(label + Config.DOCKER_HOST_SUFFIX);
            String certificateProperty = getOvercastProperty(label + Config.DOCKER_CERTIFICATES_SUFFIX);

            URI host = hostProperty == null ? null : new URI(hostProperty);
            Path certificates = certificateProperty == null ? null : Paths.get(certificateProperty);
            return DockerDriver.buildClient(host, certificates);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldRunLinksConfig() throws DockerException, InterruptedException {
        DockerHost greeter = (DockerHost) CloudHostFactory.getCloudHost("greeterConfig");
        assertThat(greeter, notNullValue());

        DockerHost itestHost = (DockerHost) CloudHostFactory.getCloudHost(DOCKER_LINKS_CONFIG);
        assertThat(itestHost, notNullValue());

        DockerClient dockerClient = createDockerClient(DOCKER_LINKS_CONFIG);

        try {
            greeter.setup();

            itestHost.setup();
            assertThat(itestHost.getLinks(), contains("greeter:greeter"));

            String containerId = itestHost.getDockerDriver().getContainerId();
            dockerClient.inspectContainer(containerId);

            try (final LogStream logStream = dockerClient.logs(containerId, LogsParam.stdout(), LogsParam.stderr(), LogsParam.follow())) {
                final String logs = logStream.readFully();
                assertThat(logs, containsString("hi"));
            }
        } finally {
            greeter.teardown();
            itestHost.teardown();
        }
    }

    @Test
    public void shouldRunAdvancedConfig() throws DockerException, InterruptedException {
        DockerHost itestHost = (DockerHost) CloudHostFactory.getCloudHost(DOCKER_ADVANCED_CONFIG);
        assertThat(itestHost, notNullValue());

        DockerClient dockerClient = createDockerClient(DOCKER_ADVANCED_CONFIG);

        try {
            itestHost.setup();
            assertThat(itestHost.getHostName(), equalTo("localhost"));
            assertThat(itestHost.getPort(12345), allOf(greaterThanOrEqualTo(DOCKER_PORT_RANGE_MIN), lessThanOrEqualTo(DOCKER_PORT_RANGE_MAX)));
            assertThat(itestHost.getPort(23456), allOf(greaterThanOrEqualTo(DOCKER_PORT_RANGE_MIN), lessThanOrEqualTo(DOCKER_PORT_RANGE_MAX)));
            assertThat(itestHost.getPort(34567), allOf(greaterThanOrEqualTo(DOCKER_PORT_RANGE_MIN), lessThanOrEqualTo(DOCKER_PORT_RANGE_MAX)));
            String containerId = itestHost.getDockerDriver().getContainerId();

            ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);

            assertThat(containerInfo.config().image(), equalTo("busybox:1"));
            assertThat(containerInfo.name(), equalTo("/mycontainer"));
            assertThat(containerInfo.config().env(), hasItem("MYVAR1=AAA"));
            assertThat(containerInfo.config().env(), hasItem("MYVAR2=BBB"));
            assertThat(containerInfo.config().env(), hasItem("MYVAR3=CCC"));
            assertThat(containerInfo.config().cmd(), Matchers.equalTo(Arrays.asList("/bin/sh", "-c", "while true; do echo hello world; sleep 1; done")));
            assertFalse(containerInfo.config().tty());

            dockerClient.inspectContainer(containerId);
        } finally {
            itestHost.teardown();
        }
    }

    @Test
    public void shouldRunAdvancedConfigWithTty() throws DockerException, InterruptedException {
        DockerHost itestHost = (DockerHost) CloudHostFactory.getCloudHost(DOCKER_ADVANCED_CONFIG_TTY);
        assertThat(itestHost, notNullValue());

        DockerClient dockerClient = createDockerClient(DOCKER_ADVANCED_CONFIG_TTY);

        try {
            itestHost.setup();
            String containerId = itestHost.getDockerDriver().getContainerId();
            ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);

            assertTrue(containerInfo.config().tty());
            dockerClient.inspectContainer(containerId);
        } finally {
            itestHost.teardown();
        }
    }
}
