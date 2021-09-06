/**
 *    Copyright 2012-2021 Digital.ai
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
package com.xebialabs.overcast;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.ssh.SshConnectionType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat(options.<String>get(PRIVATE_KEY_FILE), equalTo("privateKey"));
        assertThat(options.<String>get(PASSPHRASE), equalTo("mypass"));
        assertThat(options.<String>get(ADDRESS), equalTo("localhost"));
        assertThat(options.<String>get(USERNAME), equalTo("user"));
        assertThat(options.<String>get(PASSWORD), equalTo("secret"));
    }

    @Test
    public void testCopyFile() throws IOException {
        File tempdir = Files.createTempDirectory("overthere-util-test").toFile();
        try {
            OverthereConnection srcHost = LocalConnection.getLocalConnection();
            OverthereConnection dstHost = LocalConnection.getLocalConnection();

            File dstFile = new File(tempdir, "destfile");
            OverthereUtil.copyFiles(srcHost, dstHost, Arrays.asList("src/test/resources/copyFilesTest/1/file", dstFile.getAbsolutePath()));
            assertThat(dstFile.exists(), equalTo(true));
        } finally {
            deleteRecursively(tempdir);
        }
    }

    @Test
    public void testCopyDir() throws IOException {
        File tempdir = Files.createTempDirectory("overthere-util-test").toFile();
        try {
            OverthereConnection srcHost = LocalConnection.getLocalConnection();
            OverthereConnection dstHost = LocalConnection.getLocalConnection();

            OverthereUtil.copyFiles(srcHost, dstHost, Arrays.asList("src/test/resources/copyFilesTest/1", tempdir.getAbsolutePath()));

            File dstFile = new File(tempdir, "file");
            assertThat(dstFile.exists(), equalTo(true));
        } finally {
            deleteRecursively(tempdir);
        }
    }

    @Test
    public void testCopyMultiple() throws IOException {
        File tempdir = Files.createTempDirectory("overthere-util-test").toFile();
        try {
            OverthereConnection srcHost = LocalConnection.getLocalConnection();
            OverthereConnection dstHost = LocalConnection.getLocalConnection();

            OverthereUtil.copyFiles(srcHost, dstHost,
                    Arrays.asList("src/test/resources/copyFilesTest/1", "src/test/resources/copyFilesTest/2", tempdir.getAbsolutePath()));

            File dstFiles[] = new File[]{new File(tempdir, "file"), new File(tempdir, "file2")};
            for (File dstFile : dstFiles) {
                assertThat(dstFile.toString() + " should exist", dstFile.exists(), equalTo(true));
            }
        } finally {
            deleteRecursively(tempdir);
        }
    }

}
