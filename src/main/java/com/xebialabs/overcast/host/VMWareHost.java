package com.xebialabs.overcast.host;

import com.xebialabs.overcast.support.vmware.VMWareVM;
import com.xebialabs.overcast.support.vmware.VmWareApiClient;
import com.xebialabs.overcast.util.RetryCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VMWareHost implements CloudHost {

    public static Logger logger = LoggerFactory.getLogger(VMWareHost.class);

    public static final String VMWARE_API_HOST_SUFFIX = ".vmwareApiHost";

    public static final String VMWARE_MAX_RETRIES_SUFFIX = ".maxRetries";
    public static final String VMWARE_MAX_RETRIES_DEFAULT = "15";

    public static final String VMWARE_TIMEOUT_PROPERTY_SUFFIX = ".vmwareStartTimeout";
    public static final String VMWARE_TIMEOUT_DEFAULT = "180"; // 3 minutes

    public static final String VMWARE_IGNORE_BAD_CERTIFICATE_SUFFIX = ".ignoreBadCertificate";
    public static final String VMWARE_IGNORE_BAD_CERTIFICATE_DEFAULT = "false";

    public static final String VMWARE_SECURITY_ALGORITHM_SUFFIX = ".securityAlgorithm";
    public static final String VMWARE_SECURITY_ALGORITHM_DEFAULT = "TLS";

    public static final String VMWARE_VM_BASE_IMAGE_PROPERTY_SUFFIX = ".vmBaseImage";

    public static final String VMWARE_INSTANCE_CLONE_SUFFIX = ".instanceClone";
    public static final String VMWARE_INSTANCE_CLONE_DEFAULT = "true";

    private String ipAddress;

    private String vmId;

    private final String apiHost;

    private final String sessionId;

    private final String vmBaseImage;

    private final VmWareApiClient client;

    private final int maxRetries;

    public VMWareHost(String apiHost,
                      String authHash,
                      String vmBaseImage,
                      Boolean ignoreBadCertificate,
                      Boolean instanceClone,
                      String securityAlgorithm,
                      int connectionTimeout,
                      int maxRetries) {
        this.apiHost = apiHost;
        this.vmBaseImage = vmBaseImage;
        this.maxRetries = maxRetries;
        this.client = new VmWareApiClient(apiHost, instanceClone, ignoreBadCertificate, securityAlgorithm, connectionTimeout);
        this.sessionId = this.client.createSession(authHash);
    }

    @Override
    public void setup() {
        List<VMWareVM> vms = client.listVMs(sessionId);
        vms.stream()
                .filter(vm -> vm.getName().equals(vmBaseImage))
                .findFirst()
                .ifPresent(vm -> {
                    this.vmId = client.cloneVm(sessionId, vmBaseImage, vm.getId());

                    Map<String, Object> identity = new RetryCommand<Map<String, Object>>(maxRetries)
                            .run(() -> client.getGuestIdentity(sessionId, vmId));

                    ipAddress = (String) identity.get("ip_address");
                });
    }

    @Override
    public void teardown() {
        if (vmId == null) {
            logger.info("Nothing to tear down, VM [{}] wasn't created.", vmBaseImage);
            return;
        }
        boolean isOn = client.isPowerVmOn(sessionId, vmId);
        logger.info("VM [{}] power is {}", vmBaseImage, isOn);
        if (isOn) {
            client.stopPowerVm(sessionId, vmId);
            logger.info("About to stop power for VM [{}]", vmBaseImage);
        }
        logger.info("About to delete VM [{}]", vmId);
        client.deleteVm(sessionId, vmId);
    }

    @Override
    public String getHostName() {
        return ipAddress;
    }

    @Override
    public int getPort(int port) {
        return port;
    }

    public String getApiHost() {
        return apiHost;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getVmBaseImage() {
        return vmBaseImage;
    }

    private void waitTillQuery() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
