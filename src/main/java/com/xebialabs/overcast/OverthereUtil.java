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
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import static com.xebialabs.overthere.ConnectionOptions.*;

public final class OverthereUtil {

    private OverthereUtil() {
    }

    public static ConnectionOptions fromQuery(URI url) {
        ConnectionOptions options = new ConnectionOptions();
        options.set(ADDRESS, url.getHost());
        if (url.getPort() > 0) {
            options.set(PORT, url.getPort());
        }
        String user = url.getUserInfo();
        String password = null;
        if (user == null) {
            user = System.getProperty("user.name");
        } else {
            int idx = user.indexOf(':');
            if (idx != -1) {
                password = user.substring(idx + 1);
                options.set(PASSWORD, password);
                user = user.substring(0, idx);
            }
        }
        options.set(USERNAME, user);
        List<NameValuePair> nvps = URLEncodedUtils.parse(url, StandardCharsets.UTF_8);
        for (NameValuePair nvp : nvps) {
            options.set(nvp.getName(), nvp.getValue());
        }
        return options;
    }

    public static OverthereConnection overthereConnectionFromURI(String url) {
        try {
            return overthereConnectionFromURI(new URI(url));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static OverthereConnection overthereConnectionFromURI(URI url) {
        ConnectionOptions options = fromQuery(url);
        return Overthere.getConnection(url.getScheme(), options);
    }

    /**
     * Copy files from srcHost to dstHost. Files are copied using overthere. So from file to file or from directory to
     * directory. If copySpec's length is even then files/directories are copied pair wise. If copySpec's lenght is odd
     * then everything is copied into the last entry.
     */
    public static void copyFiles(OverthereConnection srcHost, OverthereConnection dstHost, List<String> copySpec) {
        if (copySpec.isEmpty()) {
            return;
        }

        if (copySpec.size() % 2 == 0) {
            Iterator<String> toCopy = copySpec.iterator();
            while (toCopy.hasNext()) {
                OverthereFile src = srcHost.getFile(toCopy.next());
                OverthereFile dst = dstHost.getFile(toCopy.next());
                src.copyTo(dst);
            }
        } else {
            List<String> srcFiles = copySpec.subList(0, copySpec.size() - 1);
            OverthereFile dst = dstHost.getFile(copySpec.get(copySpec.size() - 1));

            for (String srcFile : srcFiles) {
                OverthereFile src = srcHost.getFile(srcFile);
                src.copyTo(dst);
            }
        }
    }
}
