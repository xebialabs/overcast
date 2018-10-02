/**
 *    Copyright 2012-2018 XebiaLabs B.V.
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
