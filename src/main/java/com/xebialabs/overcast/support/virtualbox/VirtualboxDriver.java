/**
 *    Copyright 2012-2018 XebiaLabs B.V.
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

import com.xebialabs.overcast.command.CommandProcessor;

import java.util.EnumSet;

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
     * Checks if VM exists. Accepts UUID or VM name as an argument.
     */
    public boolean vmExists(final String vmOrUuid) {
        String[] lines = execute("list", "vms").split("\\s");
        for (String line : lines) {
            if (line.endsWith("{" + vmOrUuid + "}") || line.startsWith("\"" + vmOrUuid + "\""))
                return true;
        }
        return false;
    }

    /**
     * Shuts down if running, restores the snapshot and starts VM.
     */
    public void loadSnapshot(String vm, String snapshotUuid) {
        if (!EnumSet.of(POWEROFF, SAVED).contains(vmState(vm))) {
            powerOff(vm);
        }
        execute("snapshot", vm, "restore", snapshotUuid);
        start(vm);
    }

    /**
     * Shuts down if running, restores the latest snapshot and starts VM.
     */
    public void loadLatestSnapshot(final String vm) {
        String quotedId = null;
        String[] lines = execute("snapshot", vm, "list", "--machinereadable").split("\n");
        for (String line : lines) {
            if (!line.isEmpty()) {
                String[] parts = line.split("=");
                if (parts[0].equals("CurrentSnapshotUUID")) {
                    quotedId = parts[1];
                }
            }
        }

        loadSnapshot(vm, quotedId.substring(1, quotedId.length() - 1));
    }

    /**
     * Shuts down VM.
     */
    public void powerOff(final String vm) {
        execute("controlvm", vm, "poweroff");
    }

    public void start(String vm) {
        execute("startvm", vm, "--type", "headless");
    }

    /**
     * Executes custom VBoxManage command
     */
    public String execute(String... command) {
        return commandProcessor.run(aCommand("VBoxManage").withArguments(command)).getOutput();
    }

    /**
     * Sets extra data on the VM
     */
    public void setExtraData(String vm, String k, String v) {
        execute("setextradata", vm, k, v);
    }

    public String getExtraData(String vm, String k) {
        final String prefix = "Value: ";

        String v = execute("getextradata", vm, k).trim();
        return v.equals("No value set!") ? null : v.substring(prefix.length());
    }

    public void createSnapshot(String vm, String name) {
        execute("snapshot", vm, "take", name, "--description", "'Snapshot taken by Overcast.'");
    }
}
