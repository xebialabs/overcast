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
package com.xebialabs.overcast.support.vagrant;

import com.xebialabs.overcast.command.CommandResponse;
import com.xebialabs.overcast.host.VagrantCloudHost;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.xebialabs.overcast.support.vagrant.VagrantState.NOT_CREATED;
import static org.mockito.Mockito.when;

public class VagrantCloudHostTest {

    @Mock
    private VagrantDriver vagrantDriver;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldThrowNoExceptionsWhenAllGoesFine() {
        when(vagrantDriver.state("vm")).thenReturn(NOT_CREATED);
        when(vagrantDriver.doVagrant("vm", "up")).thenReturn(new CommandResponse(0, "", ""));

        VagrantCloudHost vagrantCloudHost = new VagrantCloudHost("vm", "127.0.0.1", vagrantDriver);

        vagrantCloudHost.setup();
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowAnExceptionWhenExitCodeIsNot0() {
        when(vagrantDriver.doVagrant("vm", "status")).thenReturn(new CommandResponse(0, "", "not created"));
        when(vagrantDriver.doVagrant("vm", "up")).thenReturn(new CommandResponse(3, "", ""));

        VagrantCloudHost vagrantCloudHost = new VagrantCloudHost("vm", "127.0.0.1", vagrantDriver);
        vagrantCloudHost.setup();
    }


    @Test(expected = RuntimeException.class)
    public void shouldThrowAnExceptionWhenOutputContainsPuppetErrors() {

        String outputWithPuppetErrors = "\u001B[0;36mnotice: /Stage[main]/Deployit-mw::blablabla\n" +
                    "\u001B[1;35merr: /Stage[main]/Was::Config/Exec[call url]/returns: blablabla[0m\n" +
                    "\u001B[0;36mnotice: /Stage[main]/Was::Config/Exec[configure-was]: Dependency blablabla[0m\n" +
                    "\u001B[0;33mwarning: /Stage[main]/Was::Config/Exec[configure-was]: Skipping because of blablabla[0m\n" +
                    "\u001B[0;36mnotice: Finished catalog run in 593.85 seconds[0m";

        when(vagrantDriver.doVagrant("status")).thenReturn(new CommandResponse(0, "", "not created"));
        when(vagrantDriver.doVagrant("up")).thenReturn(new CommandResponse(0, "", outputWithPuppetErrors));

        VagrantCloudHost vagrantCloudHost = new VagrantCloudHost("vm", "127.0.0.1", vagrantDriver);
        vagrantCloudHost.setup();
    }

}
