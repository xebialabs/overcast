package com.xebialabs.overcast.support.virtualbox;

import java.util.ArrayList;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;

import com.xebialabs.overcast.command.CommandProcessor;

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
    public VirtualboxState vmState(String vmUuid) {
        return VirtualboxState.fromStatusString(execute("showvminfo", vmUuid));
    }

    /**
     * Checks if VM exists.
     */
    public boolean vmExists(final String uuid) {
        ArrayList<String> lines = newArrayList(Splitter.on("\n").split(execute("list", "vms")));
        return filter(lines, new Predicate<String>() {
            @Override
            public boolean apply(final String input) {
                return input.endsWith("{" + uuid + "}");
            }
        }).size() == 1;
    }

    /**
     * Shuts down if running, restores the snapshot and starts VM.
     */
    public void loadSnapshot(final String vmUuid, final String snapshotUuid) {
        if (!newHashSet(POWEROFF, SAVED).contains(vmState(vmUuid))) {
            powerOff(vmUuid);
        }
        execute("snapshot", vmUuid, "restore", snapshotUuid);
        execute("startvm", vmUuid);
    }

    /**
     * Shuts down VM.
     */
    public void powerOff(final String vmUuid) {
        execute("controlvm", vmUuid, "poweroff");
    }

    /**
     * Executes custom VBoxManage command
     */
    public String execute(String... command) {
        return commandProcessor.run(aCommand("VBoxManage").withArguments(command)).getOutput();
    }
}
