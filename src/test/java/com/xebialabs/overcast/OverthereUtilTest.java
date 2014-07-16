package com.xebialabs.overcast;

import java.io.File;
import java.net.URI;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.local.LocalConnection;
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

    public void deleteRecursively(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteRecursively(c);
            }
        }
        f.delete();
    }

    @Test
    public void testGetOvercastProperty() throws Exception {
        String url = "ssh://user:secret@localhost?os=UNIX&connectionType=SFTP&privateKeyFile=privateKey&passphrase=mypass";
        ConnectionOptions options = OverthereUtil.fromQuery(new URI(url));

        assertThat(options.getEnum(OPERATING_SYSTEM, OperatingSystemFamily.class), equalTo(UNIX));
        assertThat(options.getEnum(CONNECTION_TYPE, SshConnectionType.class), equalTo(SFTP));
        assertThat(options.<String> get(PRIVATE_KEY_FILE), equalTo("privateKey"));
        assertThat(options.<String> get(PASSPHRASE), equalTo("mypass"));
        assertThat(options.<String> get(ADDRESS), equalTo("localhost"));
        assertThat(options.<String> get(USERNAME), equalTo("user"));
        assertThat(options.<String> get(PASSWORD), equalTo("secret"));
    }

    @Test
    public void testCopyFile() {
        File tempdir = Files.createTempDir();
        try {
            OverthereConnection srcHost = LocalConnection.getLocalConnection();
            OverthereConnection dstHost = LocalConnection.getLocalConnection();

            File dstFile = new File(tempdir, "destfile");
            OverthereUtil.copyFiles(srcHost, dstHost, Lists.newArrayList("src/test/resources/copyFilesTest/1/file", dstFile.getAbsolutePath()));
            assertThat(dstFile.exists(), equalTo(true));
        } finally {
            deleteRecursively(tempdir);
        }
    }

    @Test
    public void testCopyDir() {
        File tempdir = Files.createTempDir();
        try {
            OverthereConnection srcHost = LocalConnection.getLocalConnection();
            OverthereConnection dstHost = LocalConnection.getLocalConnection();

            OverthereUtil.copyFiles(srcHost, dstHost, Lists.newArrayList("src/test/resources/copyFilesTest/1", tempdir.getAbsolutePath()));

            File dstFile = new File(tempdir, "file");
            assertThat(dstFile.exists(), equalTo(true));
        } finally {
            deleteRecursively(tempdir);
        }
    }

    @Test
    public void testCopyMultiple() {
        File tempdir = Files.createTempDir();
        try {
            OverthereConnection srcHost = LocalConnection.getLocalConnection();
            OverthereConnection dstHost = LocalConnection.getLocalConnection();

            OverthereUtil.copyFiles(srcHost, dstHost,
                Lists.newArrayList("src/test/resources/copyFilesTest/1", "src/test/resources/copyFilesTest/2", tempdir.getAbsolutePath()));

            File dstFiles[] = new File[] { new File(tempdir, "file"), new File(tempdir, "file2") };
            for (File dstFile : dstFiles) {
                assertThat(dstFile.toString() + " should exist", dstFile.exists(), equalTo(true));
            }
        } finally {
            deleteRecursively(tempdir);
        }
    }

}
