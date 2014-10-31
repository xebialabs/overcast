/**
 *    Copyright 2014 XebiaLabs
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
package com.xebialabs.overcast.support.virtualbox;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.xebialabs.overcast.command.Command;
import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.command.CommandResponse;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VirtualboxDriverTest {


    @Mock
    private CommandProcessor commandProcessor;

    private VirtualboxDriver driver;

    public static final CommandResponse silentSuccess = new CommandResponse(0, "", "");
    public static final CommandResponse runningResponse = new CommandResponse(0, "", "State:           running");
    public static final CommandResponse savedResponse = new CommandResponse(0, "", "State:           saved");
    public static final CommandResponse powerOffResponse = new CommandResponse(0, "", "State:           powered off");
    private static final CommandResponse listResponse = new CommandResponse(0, "", "\"windows12\" {4407a6e4-c966-49d4-959a-50c87fffa0ac}");
    private static final CommandResponse snapshotListResponse = new CommandResponse(0, "", "SnapshotName=\"Snapshot 1\"\n" +
            "SnapshotUUID=\"baf3d83d-eb55-4733-a215-450cf090cc77\"\n" +
            "CurrentSnapshotName=\"Snapshot 1\"\n" +
            "CurrentSnapshotUUID=\"baf3d83d-eb55-4733-a215-450cf090cc77\"\n" +
            "CurrentSnapshotNode=\"SnapshotName\"\n" +
            "SnapshotName-1=\"Snapshot 2\"\n" +
            "SnapshotUUID-1=\"baf3d83d-eb55-4733-a215-450cf090cc77\"");

    private static final Command vmInfo = Command.fromString("VBoxManage showvminfo 4407a6e4-c966-49d4-959a-50c87fffa0ac");
    private static final Command powerOff = Command.fromString("VBoxManage controlvm 4407a6e4-c966-49d4-959a-50c87fffa0ac poweroff");
    private static final Command restore = Command.fromString("VBoxManage snapshot 4407a6e4-c966-49d4-959a-50c87fffa0ac restore baf3d83d-eb55-4733-a215-450cf090cc77");
    private static final Command start = Command.fromString("VBoxManage startvm 4407a6e4-c966-49d4-959a-50c87fffa0ac --type headless");
    private static final Command snapshotList = Command.fromString("VBoxManage snapshot 4407a6e4-c966-49d4-959a-50c87fffa0ac list --machinereadable");
    private static final Command getExtraData = Command.fromString("VBoxManage getextradata windows12 someKey");



    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(commandProcessor.run(Command.fromString("VBoxManage list vms"))).thenReturn(listResponse);
        driver = new VirtualboxDriver(commandProcessor);
    }

    @Test
    public void shouldKnowIfVmExists() {
        assertTrue(driver.vmExists("4407a6e4-c966-49d4-959a-50c87fffa0ac"));
        assertTrue(driver.vmExists("windows12"));
        assertFalse(driver.vmExists("8E33A1CF-767B-4F64-A544-34B2A71ABBDA"));
        assertFalse(driver.vmExists("windows13"));
    }

    @Test
    public void shouldRestoreSnapshotFromRunningState() {
        when(commandProcessor.run(vmInfo)).thenReturn(runningResponse);
        when(commandProcessor.run(powerOff)).thenReturn(silentSuccess);
        when(commandProcessor.run(restore)).thenReturn(silentSuccess);
        when(commandProcessor.run(start)).thenReturn(silentSuccess);

        driver.loadSnapshot("4407a6e4-c966-49d4-959a-50c87fffa0ac", "baf3d83d-eb55-4733-a215-450cf090cc77");

        InOrder inOrder = inOrder(commandProcessor);
        inOrder.verify(commandProcessor).run(powerOff);
        inOrder.verify(commandProcessor).run(restore);
        inOrder.verify(commandProcessor).run(start);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldRestoreSnapshotFromSavedState() {
        when(commandProcessor.run(vmInfo)).thenReturn(savedResponse);
        when(commandProcessor.run(restore)).thenReturn(silentSuccess);
        when(commandProcessor.run(start)).thenReturn(silentSuccess);

        driver.loadSnapshot("4407a6e4-c966-49d4-959a-50c87fffa0ac", "baf3d83d-eb55-4733-a215-450cf090cc77");

        InOrder inOrder = inOrder(commandProcessor);
        inOrder.verify(commandProcessor).run(restore);
        inOrder.verify(commandProcessor).run(start);
        inOrder.verifyNoMoreInteractions();

        verify(commandProcessor, never()).run(powerOff);
    }

    @Test
    public void shouldRestoreLatestSnapshot() {
        when(commandProcessor.run(snapshotList)).thenReturn(snapshotListResponse);
        when(commandProcessor.run(vmInfo)).thenReturn(savedResponse);
        when(commandProcessor.run(restore)).thenReturn(silentSuccess);
        when(commandProcessor.run(start)).thenReturn(silentSuccess);

        driver.loadLatestSnapshot("4407a6e4-c966-49d4-959a-50c87fffa0ac");

        InOrder inOrder = inOrder(commandProcessor);
        inOrder.verify(commandProcessor).run(restore);
        inOrder.verify(commandProcessor).run(start);
        inOrder.verifyNoMoreInteractions();

    }

    @Test
    public void shouldNotPowerOffIfAlreadyOff() {
        when(commandProcessor.run(vmInfo)).thenReturn(powerOffResponse);
        when(commandProcessor.run(restore)).thenReturn(silentSuccess);
        when(commandProcessor.run(start)).thenReturn(silentSuccess);

        new VirtualboxDriver(commandProcessor).loadSnapshot("4407a6e4-c966-49d4-959a-50c87fffa0ac", "baf3d83d-eb55-4733-a215-450cf090cc77");

        InOrder inOrder = inOrder(commandProcessor);
        inOrder.verify(commandProcessor).run(restore);
        inOrder.verify(commandProcessor).run(start);
        inOrder.verifyNoMoreInteractions();

        verify(commandProcessor, never()).run(powerOff);
    }

    @Test
    public void shouldReturnNullWhenExtraDataIsEmpty() {
        when(commandProcessor.run(getExtraData)).thenReturn(new CommandResponse(0, "", "No value set!"));
        assertNull(driver.getExtraData("windows12", "someKey"));
    }

    @Test
    public void shouldReturnValueWhenExtraDataIsSet() {
        when(commandProcessor.run(getExtraData)).thenReturn(new CommandResponse(0, "", "Value: some value\n"));
        assertThat(driver.getExtraData("windows12", "someKey"), is("some value"));
    }

}
