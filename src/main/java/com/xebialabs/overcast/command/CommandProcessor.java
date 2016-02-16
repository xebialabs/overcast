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
package com.xebialabs.overcast.command;

import java.io.*;
import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandProcessor {

    public static Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    private String execDir = ".";

    private CommandProcessor(final String execDir) {
        this.execDir = execDir;
    }

    private CommandProcessor() {}

    public static CommandProcessor atLocation(String l) {
        return new CommandProcessor(l);
    }

    public static CommandProcessor atCurrentDir() {
        return new CommandProcessor();
    }

    public CommandResponse run(final Command command) {

        logger.debug("Executing command {}", command);

        try {
            Process p = new ProcessBuilder(command.asList()).directory(new File(execDir)).start();

            // We do this small trick to have stdout and stderr of the process on the console and
            // at the same time capture them to strings.
            ByteArrayOutputStream errors = new ByteArrayOutputStream();
            ByteArrayOutputStream messages = new ByteArrayOutputStream();

            Thread t1 = showProcessOutput(new TeeInputStream(p.getErrorStream(), errors), System.err);
            Thread t2 = showProcessOutput(new TeeInputStream(p.getInputStream(), messages), System.out);

            int code = p.waitFor();

            t1.join();
            t2.join();

            CommandResponse response = new CommandResponse(code, errors.toString(), messages.toString());

            if (!response.isSuccessful()) {
                throw new NonZeroCodeException(command, response);
            }

            return response;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Cannot execute " + command.toString(), e);
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute " + command.toString(), e);
        }
    }

    private Thread showProcessOutput(final InputStream from, final PrintStream to) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    for (; ; ) {
                        int c = from.read();
                        if (c == -1)
                            break;
                        to.write((char) c);
                    }
                } catch (IOException ignore) {
                }
            }
        });
        t.start();
        return t;
    }

}
