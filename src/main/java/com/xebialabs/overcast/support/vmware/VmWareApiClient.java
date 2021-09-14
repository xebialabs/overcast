package com.xebialabs.overcast.support.vmware;

import org.apache.commons.lang.RandomStringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VmWareApiClient {

    private final String apiHost;
    private final int connectionTimeout;
    private final HttpClient httpClient;
    private final Boolean ignoreBadCertificate;
    private final Boolean instantClone;
    private final String securityAlgorithm;

    private static final String CLONE_VM_URL_PATH = "api/vcenter/vm?action=clone";

    private static final String CREATE_SESSION_URL_PATH = "api/session";

    private static final String DELETE_VM_URL_PATH = "api/vcenter/vm/{0}";

    private static final String GET_POWER_URL_PATH = "api/vcenter/vm/{0}/power";

    private static final String GUEST_IDENTITY_URL_PATH = "api/vcenter/vm/{0}/guest/identity";

    private static final String INSTANT_CLONE_VM_URL_PATH = "api/vcenter/vm?action=instant-clone";

    private static final String LIST_VM_URL_PATH = "api/vcenter/vm";

    private static final String SESSION_ID_HEADER = "vmware-api-session-id";

    private static final String START_POWER_URL_PATH = "api/vcenter/vm/{0}/power?action=start";

    private static final String STOP_POWER_URL_PATH = "api/vcenter/vm/{0}/power?action=stop";

    public VmWareApiClient(String apiHost,
                           Boolean instantClone,
                           Boolean ignoreBadCertificate,
                           String securityAlgorithm,
                           int connectionTimeout) {
        this.apiHost = apiHost;
        this.ignoreBadCertificate = ignoreBadCertificate;
        this.instantClone = instantClone;
        this.securityAlgorithm = securityAlgorithm;
        this.connectionTimeout = connectionTimeout;
        this.httpClient = buildClient();
    }

    public String createSession(String authHash) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(toUri(apiHost, CREATE_SESSION_URL_PATH))
                .header("Authorization", "Basic " + authHash)
                .header("vmware-use-header-authn", "string")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .timeout(Duration.ofSeconds(connectionTimeout))
                .build();

        String body = send(request, HttpResponse.BodyHandlers.ofString())
                .body();

        return noQuotes(body);
    }

    public List<VMWareVM> listVMs(String sessionId) {
        HttpRequest request = createGetRequest(sessionId, LIST_VM_URL_PATH);

        HttpResponse<Supplier<List>> response = send(request, new JsonBodyHandler<>(List.class));
        List<Map<String, Object>> result = response.body().get();

        return result
                .stream()
                .map(item ->
                        new VMWareVM(
                                Integer.parseInt(item.get("memory_size_MiB").toString()),
                                item.get("vm").toString(),
                                item.get("name").toString(),
                                item.get("power_state").toString(),
                                Integer.parseInt(item.get("cpu_count").toString())
                        )
                ).collect(Collectors.toUnmodifiableList());
    }

    public boolean isPowerVmOn(String sessionId, String vmId) {
        HttpRequest request = createGetRequest(sessionId, MessageFormat.format(GET_POWER_URL_PATH, vmId));

        HttpResponse<Supplier<Map>> response = send(request, new JsonBodyHandler<>(Map.class));
        Map<String, Object> result = response.body().get();

        return result.get("state").equals("POWERED_ON");
    }

    public void startPowerVm(String sessionId, String vmId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(toUri(apiHost, MessageFormat.format(START_POWER_URL_PATH, vmId)))
                .header(SESSION_ID_HEADER, sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .timeout(Duration.ofSeconds(connectionTimeout))
                .build();

        send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void stopPowerVm(String sessionId, String vmId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(toUri(apiHost, MessageFormat.format(STOP_POWER_URL_PATH, vmId)))
                .header(SESSION_ID_HEADER, sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .timeout(Duration.ofSeconds(connectionTimeout))
                .build();

        send(request, HttpResponse.BodyHandlers.ofString());
    }

    public Map<String, Object> getGuestIdentity(String sessionId, String vmId) {
        HttpRequest request = createGetRequest(sessionId, MessageFormat.format(GUEST_IDENTITY_URL_PATH, vmId));

        HttpResponse<Supplier<Map>> response = send(request, new JsonBodyHandler<>(Map.class));
        Map<String, Object> body = response.body().get();

        if (body.get("error_type") != null) {
            throw new IllegalStateException("Service is not ready, error_type is " + body.get("error_type"));
        }

        return body;
    }

    private HttpRequest createGetRequest(String sessionId, String relativeUrlPath) {
        return HttpRequest.newBuilder()
                .uri(toUri(apiHost, relativeUrlPath))
                .header(SESSION_ID_HEADER, sessionId)
                .GET()
                .timeout(Duration.ofSeconds(connectionTimeout))
                .build();
    }

    public String cloneVm(String sessionId, String vmBaseImage, String vmId) {
        String clonedVmName = String.format("%s-%s", vmBaseImage, RandomStringUtils.randomAlphanumeric(8));

        if (!isPowerVmOn(sessionId, vmId) && instantClone) {
            throw new IllegalStateException(String.format("You can't instant clone from VM [%s] which is powered off", vmBaseImage));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(toUri(apiHost, instantClone ? INSTANT_CLONE_VM_URL_PATH : CLONE_VM_URL_PATH))
                .header("Content-Type", "application/json")
                .header(SESSION_ID_HEADER, sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(
                        String.format("{\"name\": \"%s\", \"source\": \"%s\"}", clonedVmName, vmId)))
                .timeout(Duration.ofSeconds(connectionTimeout))
                .build();

        String body = send(request, HttpResponse.BodyHandlers.ofString()).body();
        return noQuotes(body);
    }

    public void deleteVm(String sessionId, String vmId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(toUri(apiHost, MessageFormat.format(DELETE_VM_URL_PATH, vmId)))
                .header(SESSION_ID_HEADER, sessionId)
                .DELETE()
                .timeout(Duration.ofSeconds(connectionTimeout))
                .build();
        send(request, HttpResponse.BodyHandlers.discarding());
    }

    private <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        HttpResponse<T> response;
        try {
            response = httpClient.send(request, responseBodyHandler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private URI toUri(String host, String relativePath) {
        String serverHost = host.endsWith("/") ? host : host + "/";
        return URI.create(serverHost + relativePath);
    }

    private HttpClient buildClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(connectionTimeout));

        if (ignoreBadCertificate) {
            try {
                SSLContext sslContext = SSLContext.getInstance(securityAlgorithm);
                sslContext.init(null, trustAllCerts, new SecureRandom());
                builder.sslContext(sslContext);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return builder.build();
    }

    private final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };

    private String noQuotes(String str) {
        return str.replaceAll("\"", "");
    }
}
