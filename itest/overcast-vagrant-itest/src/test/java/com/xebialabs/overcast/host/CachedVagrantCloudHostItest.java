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
import com.xebialabs.overcast.support.vagrant.VagrantDriver;
import com.xebialabs.overcast.support.virtualbox.VirtualboxDriver;
import com.xebialabs.overcast.support.virtualbox.VirtualboxState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CachedVagrantCloudHostItest {

    public static final String VM = "overcast_itest";
    private VirtualboxDriver vbox;
    private VagrantDriver vagrant;
    private CommandProcessor cmd;

    @BeforeEach
    public void before() {
        cmd = CommandProcessor.atLocation("./box");
        vagrant = new VagrantDriver(VM, cmd);
        vbox = new VirtualboxDriver(cmd);
    }

    @AfterEach
    public void after() {
        vagrant.doVagrant(VM, "destroy", "-f");
    }

    @Test
    public void shouldSetUpAndTearDownTheVm() {
        CloudHost h = CloudHostFactory.getCloudHost("overcastVagrantHost");
        h.setup();

        assertThat(vbox.vmExists(VM), is(true));
        assertThat(vbox.vmState(VM), is(VirtualboxState.RUNNING));

        h.teardown();

        assertThat(vbox.vmState(VM), is(VirtualboxState.POWEROFF));
        assertThat(vbox.getExtraData(VM, CachedVagrantCloudHost.EXPIRATION_TAG_PROPERTY_KEY), is("'ohai'"));

        h.setup();

        assertThat(vbox.vmState(VM), is(VirtualboxState.RUNNING));
    }
}
