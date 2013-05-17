package com.xebialabs.overcast.support.vagrant;

import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.command.CommandResponse;

import static com.xebialabs.overcast.command.Command.aCommand;

public class VagrantDriver {

    private String hostLabel;
    private CommandProcessor commandProcessor;

    public VagrantDriver(String hostLabel, CommandProcessor commandProcessor) {
        this.hostLabel = hostLabel;
        this.commandProcessor = commandProcessor;
    }


    /**
     * Executes vagrant command which means that arguments passed here will be prepended with "vagrant"
     * @param vagrantCommand arguments for <i><vagrant</i> command
     * @return vagrant response object
     */
    public CommandResponse doVagrant(String vagrantVm, final String... vagrantCommand) {
        CommandResponse response = commandProcessor.run(
                aCommand("vagrant").withArguments(vagrantCommand).withOptions(vagrantVm)
        );

        // Check for puppet errors. Not vagrant still returns 0 when puppet fails
        // May not be needed after this PR released: https://github.com/mitchellh/vagrant/pull/1175
        for (String line : response.getOutput().split("\n\u001B")) {
            if (line.startsWith("[1;35merr:")) {
                throw new RuntimeException("Error in puppet output: " + line);
            }
        }

        return response;
    }

    public CommandResponse status(String vm) {
        return doVagrant(vm, "status");
    }

    @Override
    public String toString() {
        return hostLabel;
    }

}
