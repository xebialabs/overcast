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
package com.xebialabs.overcast.host;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.xebialabs.overcast.command.Command;
import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.command.CommandResponse;
import com.xebialabs.overcast.command.NonZeroCodeException;
import com.xebialabs.overcast.support.vagrant.VagrantDriver;
import com.xebialabs.overcast.support.vagrant.VagrantState;
import com.xebialabs.overcast.support.virtualbox.VirtualboxDriver;
import com.xebialabs.overcast.support.virtualbox.VirtualboxState;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;

import static com.xebialabs.overcast.host.CachedVagrantCloudHost.EXPIRATION_TAG_PROPERTY_KEY;
import static com.xebialabs.overcast.support.vagrant.VagrantState.NOT_CREATED;
import static com.xebialabs.overcast.support.vagrant.VagrantState.POWEROFF;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CachedVagrantCloudHostTest {

    public static final String SOME_SHA = "ef65erfds-i-am-git-SHA-bk34hg";
    public static final String SOME_OTHER_SHA = "uygw4-i-am-git-SHA-k34h";

    public static final CommandResponse SOME_SHA_RESPONSE = new CommandResponse(0, "", SOME_SHA + "\n");
    public static final CommandResponse FAILED_RESPONSE = new CommandResponse(2, "", "");
    public static final CommandResponse OK_RESPONSE = new CommandResponse(0, "", "");

    @Mock
    private VagrantDriver vagrantDriver;

    @Mock
    private OverthereConnectionBuilder cb;

    @Mock
    private OverthereConnection connection;

    @Mock
    private VirtualboxDriver virtualboxDriver;

    @Mock
    private CommandProcessor commandProcessor;

    private CachedVagrantCloudHost cloudHost;

    private Command myCommand;

    @Before
    public void setUp() {
        initMocks(this);

        when(cb.connect()).thenReturn(connection);

        myCommand = Command.fromString("my-command");
        cloudHost = new CachedVagrantCloudHost("myvm", "127.0.0.1", myCommand, vagrantDriver, virtualboxDriver, commandProcessor, cb);
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailIfExpirationCommandFails() {
        when(commandProcessor.run(myCommand)).thenThrow(new NonZeroCodeException(myCommand, FAILED_RESPONSE));
        cloudHost.setup();
    }

    @Test
    public void shouldVagrantUpCreateSnapshotAndSetTagIfVmDoesNotExist() {
        // Happy expiration command execution
        when(commandProcessor.run(myCommand)).thenReturn(SOME_SHA_RESPONSE);

        // Happy vagrant upping
        when(vagrantDriver.state("myvm")).thenReturn(VagrantState.NOT_CREATED);
        when(vagrantDriver.doVagrant("myvm", "up")).thenReturn(OK_RESPONSE);

        cloudHost.setup();

        InOrder inOrder = inOrder(vagrantDriver, virtualboxDriver);
        inOrder.verify(vagrantDriver).doVagrant("myvm", "up", "--provision");
        inOrder.verify(virtualboxDriver).setExtraData("myvm", EXPIRATION_TAG_PROPERTY_KEY, SOME_SHA);
        inOrder.verify(virtualboxDriver).createSnapshot("myvm", SOME_SHA);
    }

    @Test
    public void shouldDestroyAndVagrantUpIfTheTagDiffers() {
        // Happy expiration command execution
        when(commandProcessor.run(myCommand)).thenReturn(SOME_SHA_RESPONSE);

        when(vagrantDriver.state("myvm")).thenReturn(POWEROFF).thenReturn(NOT_CREATED);
        when(virtualboxDriver.getExtraData("myvm", EXPIRATION_TAG_PROPERTY_KEY)).thenReturn(SOME_OTHER_SHA);

        cloudHost.setup();

        InOrder inOrder = inOrder(vagrantDriver, virtualboxDriver);
        inOrder.verify(vagrantDriver).doVagrant("myvm", "destroy", "-f");
        inOrder.verify(vagrantDriver).doVagrant("myvm", "up", "--provision");
        inOrder.verify(virtualboxDriver).setExtraData("myvm", EXPIRATION_TAG_PROPERTY_KEY, SOME_SHA);
        inOrder.verify(virtualboxDriver).createSnapshot("myvm", SOME_SHA);
    }

    @Test
    public void shouldLoadTheSnapshotIfTheTagMatches() {
        when(commandProcessor.run(myCommand)).thenReturn(SOME_SHA_RESPONSE);
        when(vagrantDriver.state("myvm")).thenReturn(POWEROFF);
        when(virtualboxDriver.getExtraData("myvm", EXPIRATION_TAG_PROPERTY_KEY)).thenReturn(SOME_SHA);

        cloudHost.setup();

        verify(vagrantDriver, never()).doVagrant(anyString(), anyString());
        verify(virtualboxDriver).loadLatestSnapshot("myvm");
    }

    @Test
    public void shouldCreateSnapshotAndPowerOffWhenTagExists() {
        when(virtualboxDriver.getExtraData("myvm", EXPIRATION_TAG_PROPERTY_KEY)).thenReturn(SOME_OTHER_SHA);
        when(virtualboxDriver.vmState("myvm")).thenReturn(VirtualboxState.RUNNING);

        cloudHost.teardown();

        verify(virtualboxDriver).powerOff("myvm");

        // Tag is already set and we not gonna update it
        verify(commandProcessor, never()).run(myCommand);

        // We should do the stuff via VBoxManage only
        verify(vagrantDriver, never()).doVagrant(anyString(), anyString());
    }

    @Test
    public void shouldNotPowerOffWhenAborted() {
        when(virtualboxDriver.vmState("myvm")).thenReturn(VirtualboxState.ABORTED);

        cloudHost.teardown();

        verify(virtualboxDriver, never()).powerOff("myvm");
        verify(vagrantDriver, never()).doVagrant(anyString(), anyString());
    }

    @Test
    public void shouldNotPowerOffWhenOff() {
        when(virtualboxDriver.vmState("myvm")).thenReturn(VirtualboxState.POWEROFF);

        cloudHost.teardown();

        verify(virtualboxDriver, never()).powerOff("myvm");
        verify(vagrantDriver, never()).doVagrant(anyString(), anyString());
    }

    @Test
    public void shouldPutIntoInitialStateWithVagrantWhenTagDoesNotExist() {
        when(virtualboxDriver.getExtraData("myvm", EXPIRATION_TAG_PROPERTY_KEY)).thenReturn(null);
        when(vagrantDriver.state("myvm")).thenReturn(VagrantState.RUNNING);

        VagrantCloudHost ch = new CachedVagrantCloudHost("myvm", "127.0.0.1", myCommand, vagrantDriver, virtualboxDriver, commandProcessor, cb);
        ch.teardown();

        verify(vagrantDriver).doVagrant("myvm", "destroy", "-f");
        verify(virtualboxDriver, never()).powerOff("myvm");
    }
}
