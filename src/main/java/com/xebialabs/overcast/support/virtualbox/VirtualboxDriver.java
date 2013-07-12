package com.xebialabs.overcast.support.virtualbox;

import java.util.ArrayList;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;

import com.xebialabs.overcast.command.CommandProcessor;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.xebialabs.overcast.command.Command.aCommand;
import static com.xebialabs.overcast.support.virtualbox.VirtualboxState.POWEROFF;
import static com.xebialabs.overcast.support.virtualbox.VirtualboxState.SAVED;

public class VirtualboxDriver {

    private CommandProcessor commandProcessor;

    public VirtualboxDriver(final CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    /**
     * Fetches VM state.
     */
    public VirtualboxState vmState(String vm) {
        return VirtualboxState.fromStatusString(execute("showvminfo", vm));
    }

    /**
     * Checks if VM exists.
     */
    public boolean vmExists(final String vm) {
        ArrayList<String> lines = newArrayList(Splitter.on("\n").split(execute("list", "vms")));
        return filter(lines, new Predicate<String>() {
            @Override
            public boolean apply(final String input) {
                return input.endsWith("{" + vm + "}");
            }
        }).size() == 1;
    }

    /**
     * Shuts down if running, restores the snapshot and starts VM.
     */
    public void loadSnapshot(final String vm, final String snapshotUuid) {
        if (!newHashSet(POWEROFF, SAVED).contains(vmState(vm))) {
            powerOff(vm);
        }
        execute("snapshot", vm, "restore", snapshotUuid);
        execute("startvm", vm);
    }

    /**
     * Shuts down VM.
     */
    public void powerOff(final String vm) {
        execute("controlvm", vm, "poweroff");
    }

    /**
     * Executes custom VBoxManage command
     */
    public String execute(String... command) {
        return commandProcessor.run(aCommand("VBoxManage").withArguments(command)).getOutput();
    }

    /**
     * Sets extra data on the VM
     * @param vm
     * @param expirationTagPropertyKey
     * @param someSha
     */
    public void setExtraData(final String vm, final String expirationTagPropertyKey, final String someSha) {
        throw new NotImplementedException();
    }

    public String getExtraData(final String vm, final String expirationTagPropertyKey) {
        throw new NotImplementedException();
    }
}
