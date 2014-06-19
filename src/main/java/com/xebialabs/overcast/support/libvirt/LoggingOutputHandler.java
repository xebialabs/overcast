package com.xebialabs.overcast.support.libvirt;

import org.slf4j.Logger;

import com.xebialabs.overthere.OverthereExecutionOutputHandler;

public class LoggingOutputHandler implements OverthereExecutionOutputHandler {
    private final Logger logger;
    private final String prefix;

    public LoggingOutputHandler(Logger logger, String prefix) {
        this.logger = logger;
        this.prefix = prefix;
    }

    @Override
    public void handleChar(char c) {
        // empty
    }

    @Override
    public void handleLine(String line) {
        logger.info("[{}] {}", prefix, line);
    }
}
