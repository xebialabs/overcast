/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overcast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Map;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import static com.google.common.base.Preconditions.checkArgument;

class TunneledCloudHost implements CloudHost {

	private final CloudHost actualHost;
	private final String username;
	private final String password;
	private final Map<Integer, Integer> portForwardMap;

	private SSHClient client;

	TunneledCloudHost(CloudHost actualHost, String username, String password, Map<Integer, Integer> portForwardMap) {
		this.actualHost = actualHost;
		this.username = username;
		this.password = password;
		this.portForwardMap = portForwardMap;
	}

	@Override
	public void setup() {
		actualHost.setup();

		client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());

		try {
			client.connect(actualHost.getHostName(), 22);
			client.authPassword(username, password);
			for (Map.Entry<Integer, Integer> forwardedPort : portForwardMap.entrySet()) {
				int remotePort = forwardedPort.getKey();
				int localPort = forwardedPort.getValue();

				final LocalPortForwarder.Parameters params = new LocalPortForwarder.Parameters("localhost", localPort, "localhost", remotePort);
				final ServerSocket ss = new ServerSocket();
				ss.setReuseAddress(true);
				ss.bind(new InetSocketAddress(params.getLocalHost(), params.getLocalPort()));

				final LocalPortForwarder forwarder = client.newLocalPortForwarder(params, ss);
				Thread forwarderThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							forwarder.listen();
						} catch (IOException ignore) {
						}
					}
				}, "SSH port forwarder thread from local port " + localPort + " to " + actualHost.getHostName() + ":" + remotePort);
				forwarderThread.setDaemon(true);
				CloudHostFactory.logger.info("Starting {}", forwarderThread.getName());
				forwarderThread.start();
			}
		} catch (IOException exc) {
			throw new RuntimeException("Cannot set up tunnels to " + actualHost.getHostName(), exc);
		}
	}

	@Override
	public void teardown() {
		try {
			client.disconnect();
		} catch (IOException ignored) {
			//
		}

		actualHost.teardown();
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

}
