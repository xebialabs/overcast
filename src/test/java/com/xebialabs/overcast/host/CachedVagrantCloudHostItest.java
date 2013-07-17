package com.xebialabs.overcast.host;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xebialabs.overcast.command.Command;
import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.support.vagrant.VagrantDriver;
import com.xebialabs.overcast.support.virtualbox.VirtualboxDriver;
import com.xebialabs.overcast.support.virtualbox.VirtualboxState;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionType;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CachedVagrantCloudHostItest {

    public static final String VM = "overcast_itest";
    public static final String IP = "10.10.200.200";
    private VirtualboxDriver vbox;
    private VagrantDriver vagrant;
    private CommandProcessor cmd;

    @Before
    public void before() {
        cmd = CommandProcessor.atLocation("./box");
        vagrant = new VagrantDriver(VM, cmd);
        vbox = new VirtualboxDriver(cmd);
    }

    @After
    public void after() {
        vagrant.doVagrant(VM, "destroy", "-f");
    }

    @Test
    public void shouldSetUpAndTearDownTheVm() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(ConnectionOptions.ADDRESS, IP);
        options.set(ConnectionOptions.USERNAME, "vagrant");
        options.set(ConnectionOptions.PASSWORD, "vagrant");
        options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
        options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SFTP);

        OverthereConnectionBuilder b = new SshConnectionBuilder("ssh", options, new DefaultAddressPortMapper());

        CachedVagrantCloudHost h = new CachedVagrantCloudHost(VM, IP, Command.fromString("echo 'ohai'"), vagrant, vbox, cmd, b);
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
