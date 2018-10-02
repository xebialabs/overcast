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

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;

import static com.xebialabs.overcast.OvercastProperties.getOvercastProperty;
import static com.xebialabs.overcast.OvercastProperties.getRequiredOvercastProperty;
import static com.xebialabs.overcast.OverthereUtil.overthereConnectionFromURI;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;

/**
 * {@link IpLookupStrategy} that uses SSH to execute a command on a remote host to look up the IP based on the MAC.
 */
public class SshIpLookupStrategy implements IpLookupStrategy {
    private static final Logger log = LoggerFactory.getLogger(SshIpLookupStrategy.class);

    private static final String SSH_TIMEOUT_SUFFIX = ".SSH.timeout";
    private static final String SSH_COMMAND_SUFFIX = ".SSH.command";
    private static final String SSH_URL_SUFFIX = ".SSH.url";

    private URI url;
    private String command;
    private int timeout;

    public SshIpLookupStrategy(URI url, String command, int timeout) {
        this.url = url;
        this.command = command;
        this.timeout = timeout;
    }

    public static SshIpLookupStrategy create(String prefix) {
        try {
            URI uri = new URI(getRequiredOvercastProperty(prefix + SSH_URL_SUFFIX));
            String command = getRequiredOvercastProperty(prefix + SSH_COMMAND_SUFFIX);
            int timeout = Integer.parseInt(getOvercastProperty(prefix + SSH_TIMEOUT_SUFFIX, "60"));
            SshIpLookupStrategy instance = new SshIpLookupStrategy(uri, command, timeout);
            return instance;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String lookup(String mac) {
        Preconditions.checkNotNull(mac, "Need a MAC to lookup the IP of a host.");

        CmdLine cmdLine = new CmdLine();
        String fragment = MessageFormat.format(command, mac);
        cmdLine.addRaw(fragment);
        log.info("Will use command '{}' to detect IP", cmdLine);

        OverthereConnection connection = null;
        try {
            connection = overthereConnectionFromURI(url);
            int seconds = timeout;
            while (seconds > 0) {
                CapturingOverthereExecutionOutputHandler outputHandler = capturingHandler();
                CapturingOverthereExecutionOutputHandler errorOutputHandler = capturingHandler();
                connection.execute(outputHandler, errorOutputHandler, cmdLine);
                if (!errorOutputHandler.getOutputLines().isEmpty()) {
                    throw new RuntimeException("Had stderror: " + errorOutputHandler.getOutput());
                }
                if (outputHandler.getOutputLines().isEmpty()) {
                    sleep(1);
                    seconds--;
                    log.debug("No IP found yet, will try for {} seconds", seconds);
                    continue;
                }
                String line = outputHandler.getOutputLines().get(0);
                log.debug("Found IP={} for MAC={}", line, mac);
                return outputHandler.getOutputLines().get(0);
            }
        } catch (RuntimeException e) {
            String message = String.format("Error looking up MAC '%s' on host '%s'", mac, url.getHost());
            log.error(message, e);
            throw new IpLookupException(message, e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        String message = String.format("No IP found for MAC '%s' on host '%s'", mac, url.getHost());
        throw new IpNotFoundException(message);
    }

    private static void sleep(final int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
