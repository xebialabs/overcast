package com.xebialabs.overcast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;

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
        List<NameValuePair> nvps = URLEncodedUtils.parse(url, "UTF-8");
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
        OverthereConnection connection = Overthere.getConnection(url.getScheme(), options);
        return connection;
    }
}
