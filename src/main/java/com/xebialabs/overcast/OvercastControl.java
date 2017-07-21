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
package com.xebialabs.overcast;

import com.xebialabs.overcast.host.CloudHost;
import com.xebialabs.overcast.host.CloudHostFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OvercastControl {

    private static final Logger logger = LoggerFactory.getLogger(OvercastControl.class);

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
    }

}
