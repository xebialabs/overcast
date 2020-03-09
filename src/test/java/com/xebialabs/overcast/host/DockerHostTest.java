/**
 *    Copyright 2012-2020 XebiaLabs B.V.
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

import org.junit.Assert;
import org.junit.Test;

public class DockerHostTest {
    @Test
    public void shouldReturnLocalhostForUnixSocket() {
        DockerHost dh = new DockerHost("image", "unix:///var/run/docker", null);
        Assert.assertEquals("localhost", dh.getHostName());
    }

    @Test
    public void shouldReturnOtherHostForUrl() {
        DockerHost dh = new DockerHost("image", "http://remotehost", null);
        Assert.assertEquals("remotehost", dh.getHostName());
    }
}
