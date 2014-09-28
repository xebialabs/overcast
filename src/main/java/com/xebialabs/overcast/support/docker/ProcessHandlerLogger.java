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
