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

/**
 * Represents a host in the cloud.
 */
public interface CloudHost {

    /**
     * Ensures the host is available.
     */
    void setup();

    /**
     * Releases the host resources.
     */
    void teardown();

    /**
     * Returns the name or IP address of the host to connect to. Can only be called after {@link #setup()} has been
     * invoked.
     *
     * @return the host name.
     */
    String getHostName();

    /**
     * Translates a target port number to the port number to connect to. Can only be called after {@link #setup()} has
     * been invoked.
     *
     * @param port
     *            the target port number
     * @return the translated port number.
     */
    int getPort(int port);
}
