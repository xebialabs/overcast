package com.xebialabs.overcast.vagrant;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.asList;

public class VagrantHelper {

    private String hostLabel;
    private String vagrantDir;
    private String vagrantVm;

    /**
     * Creates a vagrant handler
     * @param hostLabel label which is used in the overcast.properties
     * @param vagrantDir directory of the vagrant project
     * @param vagrantVm in single-vm setup can/should be null, in multi-vm setup should match vagrant VM name defined with <i>config.vm.define</i>.
     */
    public VagrantHelper(String hostLabel, String vagrantDir, String vagrantVm) {
        this.hostLabel = hostLabel;
        this.vagrantDir = vagrantDir;
        this.vagrantVm = vagrantVm;
    }

    /**
     * Executes vagrant command which means that arguments passed here will be prepended with "vagrant"
     * @param vagrantCommand arguments for <i><vagrant</i> command
     * @return vagrant response object
     */
    public VagrantResponse doVagrant(final String... vagrantCommand) {
        try {
            Process vagrant = startVagrant(vagrantCommand);
            BufferedInputStream errors = new BufferedInputStream(vagrant.getErrorStream());
            BufferedInputStream output = new BufferedInputStream(vagrant.getInputStream());
            errors.mark(Integer.MAX_VALUE);
            output.mark(Integer.MAX_VALUE);
            showProcessOutput(output, System.out);
            showProcessOutput(errors, System.err);
            int code = vagrant.waitFor();
            return new VagrantResponse(code, errors, output);
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute " + on(" ").join(vagrantCommand) + " for host " + hostLabel);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Cannot execute " + on(" ").join(vagrantCommand) + " for host " + hostLabel);
        }
    }

    @Override
    public String toString() {
        return hostLabel;
    }

    private Process startVagrant(final String... vagrantCommand) throws IOException {
        List<String> systemCommand = new ArrayList<String>(asList("vagrant", vagrantCommand));
        if (vagrantVm != null) {
            systemCommand.add(vagrantVm);
        }
        return new ProcessBuilder(systemCommand).directory(new File(vagrantDir)).start();
    }

    private void showProcessOutput(final InputStream from, final PrintStream to) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    for (; ; ) {
                        int c = from.read();
                        if (c == -1)
                            break;
                        to.write((char) c);
                    }
                } catch (IOException ignore) {
                }
            }
        });
        t.start();
    }

}
