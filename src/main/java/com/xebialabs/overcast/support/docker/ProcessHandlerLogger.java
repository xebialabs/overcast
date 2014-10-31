/**
 *    Copyright 2014 XebiaLabs
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ProgressMessage;

public class ProcessHandlerLogger implements ProgressHandler {
    @Override
    public void progress(final ProgressMessage message) throws DockerException {
        logger.info(message.id() + " " + message.status());
    }

    private static final Logger logger = LoggerFactory.getLogger(ProcessHandlerLogger.class);

}
