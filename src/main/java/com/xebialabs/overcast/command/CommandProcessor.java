package com.xebialabs.overcast.command;

import java.io.*;
import org.apache.commons.io.input.TeeInputStream;

import static java.util.Arrays.asList;

public class CommandProcessor {

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
