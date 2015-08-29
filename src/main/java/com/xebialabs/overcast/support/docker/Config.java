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
package com.xebialabs.overcast.support.docker;

public class Config {
    public static final String DOCKER_CERTIFICATES = ".certificates";
    public static final String DOCKER_HOST_SUFFIX = ".dockerHost";
    public static final String DOCKER_IMAGE_SUFFIX = ".dockerImage";
    public static final String DOCKER_NAME_SUFFIX = ".name";
    public static final String DOCKER_COMMAND_SUFFIX = ".command";
    public static final String DOCKER_REMOVE_SUFFIX = ".remove";
    public static final String DOCKER_REMOVE_VOLUME_SUFFIX = ".removeVolume";
    public static final String DOCKER_ENV_SUFFIX = ".env";
    public static final String DOCKER_EXPOSED_PORTS_SUFFIX = ".exposedPorts";

    public static final String DOCKER_EXPOSE_ALL_PORTS_SUFFIX = ".exposeAllPorts";
    public static final String DOCKER_DEFAULT_HOST = "http://localhost:2375";
    public static final String DOCKER_DEFAULT_IMAGE = "busybox";
}
