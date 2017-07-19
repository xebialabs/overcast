/**
 *    Copyright 2012-2017 XebiaLabs B.V.
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
package com.xebialabs.overcast.cli;

import com.xebialabs.overcast.host.CloudHost;
import com.xebialabs.overcast.host.CloudHostFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.kohsuke.args4j.ExampleMode.ALL;

public class OvercastCli {

    private static final Logger logger = LoggerFactory.getLogger(OvercastCli.class);

    @Option(name="-setup", usage="Setup the cloudhost")
    private List<String> setupHosts;

//    @Option(name="-wait-for-ports", usage="Wait for ports")
//    private List<Integer> waitForPorts;

    @Option(name="-teardown", usage="Teardown the cloudhost")
    private List<String> teardownHosts;

    @Option(name="-handles", usage="The handle of the host to teardown")
    private List<String> teardownHandles;

//    @Option(name="-write-properties", usage="Write properties to file")
//    private boolean writeProperties;

//    @Argument
//    private List<String> arguments = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        new OvercastCli().doMain(args);
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();

            System.err.println("  Example: java Cli"+parser.printExample(ALL));

            return;
        }

        if(setupHosts != null && !setupHosts.isEmpty()) {
            setup(setupHosts);
        } else if(teardownHosts != null && !teardownHosts.isEmpty()) {
            //teardown();
        }

    }


    public Map<String, Map<String, String>> setup(List<String> setupHosts) {
        Map<String, Map<String, String>> instances = new HashMap<>();
        for(String hostLabel : setupHosts) {
            CloudHost host = CloudHostFactory.getCloudHost(hostLabel);
            host.setup();
            String hostname = host.getHostName();
            String handle = host.getHandle();
            Map<String, String> instance = new HashMap<>();
            instance.put("hostname", hostname);
            instance.put("handle", handle);
            instances.put(hostLabel, instance);
            logger.info("Host is setup [label={}, hostname={}, handle={}]", hostLabel, hostname, handle);
        }
        return instances;
    }

    public void teardown(Map<String, Map<String, String>> instances) {

        for(Map.Entry<String, Map<String, String>> instance : instances.entrySet()) {
            String hostLabel = instance.getKey();
            String handle = instance.getValue().get("handle");
            CloudHost host = CloudHostFactory.getCloudHostByHandle(hostLabel, handle);
            logger.info("Tearing down host [label={}, handle={}]", hostLabel, handle);
            host.teardown();
        }

//        for (instance : instances.entrySet()) {
//            String handle = iterator.next();
//            CloudHost host = CloudHostFactory.getCloudHostByHandle(hostLabel, handle);
//            host.teardown();
//            logger.info("Host is teared down [label={}, handle={}]", hostLabel, host.getHandle());
//        }
    }

}
