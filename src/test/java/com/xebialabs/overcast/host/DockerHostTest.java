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

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DockerHostTest {
    @Test
    public void shouldReturnLocalhostForUnixSocket() {
        DockerHost dh = new DockerHost("image", "unix:///var/run/docker", null);
        assertThat("localhost", is(dh.getHostName()));
    }

    @Test
    public void shouldReturnOtherHostForUrl() {
        DockerHost dh = new DockerHost("image", "http://remotehost", null);
        assertThat("remotehost", is(dh.getHostName()));
    }
}
