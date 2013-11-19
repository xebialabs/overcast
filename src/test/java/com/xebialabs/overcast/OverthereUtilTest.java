package com.xebialabs.overcast;

import java.net.URI;

import org.junit.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.ssh.SshConnectionType;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PASSPHRASE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PRIVATE_KEY_FILE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class OverthereUtilTest {

    @Test
    public void testGetOvercastProperty() throws Exception {
        ConnectionOptions options = OverthereUtil.fromQuery(new URI("ssh://user:secret@localhost?os=UNIX&connectionType=SFTP&privateKeyFile=privateKey&passphrase=mypass"));

        assertThat(options.getEnum(OPERATING_SYSTEM, OperatingSystemFamily.class), equalTo(UNIX));
        assertThat(options.getEnum(CONNECTION_TYPE, SshConnectionType.class), equalTo(SFTP));
        assertThat(options.<String> get(PRIVATE_KEY_FILE), equalTo("privateKey"));
        assertThat(options.<String> get(PASSPHRASE), equalTo("mypass"));
        assertThat(options.<String> get(ADDRESS), equalTo("localhost"));
        assertThat(options.<String> get(USERNAME), equalTo("user"));
        assertThat(options.<String> get(PASSWORD), equalTo("secret"));
    }
}
