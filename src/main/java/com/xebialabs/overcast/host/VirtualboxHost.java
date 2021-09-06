/**
 *    Copyright 2012-2021 Digital.ai
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

import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.support.virtualbox.VirtualboxDriver;

public class VirtualboxHost implements CloudHost {

    private String ip, uuid, snapshot;

    public VirtualboxHost(final String ip, final String uuid, final String snapshot) {
        this.ip = ip;
        this.uuid = uuid;
        this.snapshot = snapshot;
    }

    @Override
    public void setup() {
        new VirtualboxDriver(CommandProcessor.atCurrentDir()).loadSnapshot(uuid, snapshot);
    }

    @Override
    public void teardown() {
        new VirtualboxDriver(CommandProcessor.atCurrentDir()).powerOff(uuid);
    }

    @Override
    public String getHostName() {
        return ip;
    }

    @Override
    public int getPort(final int port) {
        return port;
    }
}
