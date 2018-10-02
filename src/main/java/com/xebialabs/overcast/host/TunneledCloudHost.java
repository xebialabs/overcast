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
package com.xebialabs.overcast.host;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;

class TunneledCloudHost implements CloudHost {
    private static Logger logger = LoggerFactory.getLogger(TunneledCloudHost.class);

    private final CloudHost actualHost;
    private final String username;
    private final String password;
    private final int setupTimeout;
    private final Map<Integer, Integer> portForwardMap;

    private SSHClient client;
    private final List<PortForwarder> portForwarders;

    private static class PortForwarder {
        private final Thread thread;
        private final ServerSocket socket;

        private PortForwarder(Thread thread, ServerSocket socket) {
            this.thread = thread;
            this.socket = socket;
        }

        public String getName() {
            return thread.getName();
        }

        public static PortForwarder create(SSHClient client, String remoteHostName, String localHost, int localPort, String remoteHost, int remotePort)
            throws IOException {
            final LocalPortForwarder.Parameters params = new LocalPortForwarder.Parameters(localHost, localPort, remoteHost, remotePort);

            ServerSocket ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(localHost, localPort));

            final LocalPortForwarder forwarder = client.newLocalPortForwarder(params, ss);
            Thread forwarderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        forwarder.listen();
                    } catch (IOException ignore) {
                    }
                }
            }, "SSH port forwarder thread from local port " + localPort + " to " + remoteHostName + ":" + remotePort);
            forwarderThread.setDaemon(true);

            logger.info("Starting {}", forwarderThread.getName());
            forwarderThread.start();

            return new PortForwarder(forwarderThread, ss);
        }

        public void close() {
            try {
                thread.interrupt();
                socket.close();
            } catch (IOException e) {
                logger.debug("Ignoring exception while closing forwarding socket.", e);
            }
        }
    }

    TunneledCloudHost(CloudHost actualHost, String username, String password, Map<Integer, Integer> portForwardMap, int setupTimeout) {
        checkArgument(setupTimeout >= 0, "setupTimeout must be >= 0");
        this.actualHost = actualHost;
        this.username = username;
        this.password = password;
        this.portForwardMap = portForwardMap;
        this.setupTimeout = setupTimeout;
        this.portForwarders = Lists.newArrayList();
    }

    @Override
    public void setup() {
        actualHost.setup();

        client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        try {
            int i = setupTimeout;

            while (i >= 0) {
                try {
                    client.connect(actualHost.getHostName(), 22);
                    client.authPassword(username, password);
                    break;
                } catch (IOException e) {
                    if (!(e instanceof ConnectException || e instanceof NoRouteToHostException)) {
                        throw e;
                    }
                    logger.debug("Could not connect to '{}' to set up tunnels, retrying for {} seconds", actualHost.getHostName(), i);
                    sleep(1);
                    i--;
                }
            }
            for (Map.Entry<Integer, Integer> forwardedPort : portForwardMap.entrySet()) {
                int remotePort = forwardedPort.getKey();
                int localPort = forwardedPort.getValue();

                PortForwarder forwarder = PortForwarder.create(client, actualHost.getHostName(), "localhost", localPort, "localhost", remotePort);
                portForwarders.add(forwarder);
            }
        } catch (IOException exc) {
            throw new RuntimeException("Cannot set up tunnels to " + actualHost.getHostName(), exc);
        }
    }

    @Override
    public void teardown() {
        for (PortForwarder pf : portForwarders) {
            logger.info("Stopping portforwarder {}", pf.getName());
            pf.close();
        }

        try {
            logger.info("Disconnecting client {}", client);
            client.disconnect();
        } catch (IOException ignored) {
            //
        } finally {
            actualHost.teardown();
        }
    }

    @Override
    public String getHostName() {
        return "localhost";
    }

    @Override
    public int getPort(int port) {
        checkArgument(portForwardMap.containsKey(port), "Port %d is not tunneled", port);
        return portForwardMap.get(port);
    }

    protected static void sleep(final int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
