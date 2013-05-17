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

    private static final Command vmInfo = Command.fromString("VBoxManage showvminfo 4407a6e4-c966-49d4-959a-50c87fffa0ac");
    private static final Command powerOff = Command.fromString("VBoxManage controlvm 4407a6e4-c966-49d4-959a-50c87fffa0ac poweroff");
    private static final Command restore = Command.fromString("VBoxManage snapshot 4407a6e4-c966-49d4-959a-50c87fffa0ac restore 03e9535d-e704-492e-aec3-c2da850dd327");
    private static final Command start = Command.fromString("VBoxManage startvm 4407a6e4-c966-49d4-959a-50c87fffa0ac");



    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(commandProcessor.run(Command.fromString("VBoxManage list vms"))).thenReturn(listResponse);
        driver = new VirtualboxDriver(commandProcessor);
    }

    @Test
    public void shouldKnowIfVmExists() {
        assertTrue(driver.vmExists("4407a6e4-c966-49d4-959a-50c87fffa0ac"));
        assertFalse(driver.vmExists("8E33A1CF-767B-4F64-A544-34B2A71ABBDA"));
    }

    @Test
    public void shouldRestoreSnapshotFromRunningState() {
        when(commandProcessor.run(vmInfo)).thenReturn(runningResponse);
        when(commandProcessor.run(powerOff)).thenReturn(silentSuccess);
        when(commandProcessor.run(restore)).thenReturn(silentSuccess);
        when(commandProcessor.run(start)).thenReturn(silentSuccess);

        driver.loadSnapshot("4407a6e4-c966-49d4-959a-50c87fffa0ac", "03e9535d-e704-492e-aec3-c2da850dd327");

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

        driver.loadSnapshot("4407a6e4-c966-49d4-959a-50c87fffa0ac", "03e9535d-e704-492e-aec3-c2da850dd327");

        InOrder inOrder = inOrder(commandProcessor);
        inOrder.verify(commandProcessor).run(restore);
        inOrder.verify(commandProcessor).run(start);
        inOrder.verifyNoMoreInteractions();

        verify(commandProcessor, never()).run(powerOff);
    }

    @Test
    public void shouldNotPowerOffIfAlreadyOff() {
        when(commandProcessor.run(vmInfo)).thenReturn(powerOffResponse);
        when(commandProcessor.run(restore)).thenReturn(silentSuccess);
        when(commandProcessor.run(start)).thenReturn(silentSuccess);

        new VirtualboxDriver(commandProcessor).loadSnapshot("4407a6e4-c966-49d4-959a-50c87fffa0ac", "03e9535d-e704-492e-aec3-c2da850dd327");

        InOrder inOrder = inOrder(commandProcessor);
        inOrder.verify(commandProcessor).run(restore);
        inOrder.verify(commandProcessor).run(start);
        inOrder.verifyNoMoreInteractions();

        verify(commandProcessor, never()).run(powerOff);
    }

}
