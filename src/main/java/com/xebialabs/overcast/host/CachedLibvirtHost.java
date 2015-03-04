/**
 *    Copyright 2012-2015 XebiaLabs B.V.
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
package com.xebialabs.overcast.host;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jdom2.Document;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overcast.OverthereUtil;
import com.xebialabs.overcast.command.Command;
import com.xebialabs.overcast.command.CommandProcessor;
import com.xebialabs.overcast.command.NonZeroCodeException;
import com.xebialabs.overcast.support.libvirt.DomainWrapper;
import com.xebialabs.overcast.support.libvirt.Filesystem;
import com.xebialabs.overcast.support.libvirt.IpLookupStrategy;
import com.xebialabs.overcast.support.libvirt.LibvirtRuntimeException;
import com.xebialabs.overcast.support.libvirt.LibvirtUtil;
import com.xebialabs.overcast.support.libvirt.LoggingOutputHandler;
import com.xebialabs.overcast.support.libvirt.Metadata;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereExecutionOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overcast.OverthereUtil.overthereConnectionFromURI;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler.multiHandler;

public class CachedLibvirtHost extends LibvirtHost {
    private static final Logger logger = LoggerFactory.getLogger(CachedLibvirtHost.class);
    public static final String DEFAULT_STALE_HOST_GRACE_TIME = "" + 1 * 60 * 60 * 1000;

    public static final String PROVISION_CMD = ".provision.cmd";
    public static final String PROVISION_URL = ".provision.url";
    public static final String PROVISION_START_TIMEOUT = ".provision.startTimeout";
    public static final String PROVISION_START_TIMEOUT_DEFAULT = "60";
    public static final String COPY_SPEC = ".provision.copy";
    public static final String CACHE_EXPIRATION_CMD = ".provision.expirationTag.cmd";
    public static final String CACHE_EXPIRATION_URL = ".provision.expirationTag.url";
    public static final String PROVISIONED_BOOT_DELAY = ".provision.bootDelay";

    private final String provisionCmd;
    private final String provisionUrl;
    private final String cacheExpirationUrl;
    private final String cacheExpirationCmd;
    private CommandProcessor cmdProcessor;
    private final List<String> copySpec;

    private DomainWrapper provisionedClone;
    private String provisionedCloneIp;
    private int provisionedbootDelay;
    private int provisionStartTimeout;

    CachedLibvirtHost(String hostLabel, Connect libvirt,
        String baseDomainName, IpLookupStrategy ipLookupStrategy, String networkName,
        String provisionUrl, String provisionCmd,
        String cacheExpirationUrl, String cacheExpirationCmd,
        CommandProcessor cmdProcessor,
        int startTimeout, int bootDelay, int provisionStartTimeout, int provisionedbootDelay,
        List<Filesystem> filesystemMappings, List<String> copySpec) {
        super(libvirt, baseDomainName, ipLookupStrategy, networkName, startTimeout, bootDelay, filesystemMappings);
        this.provisionUrl = checkNotNullOrEmpty(provisionUrl, "provisionUrl");
        this.provisionCmd = checkNotNullOrEmpty(provisionCmd, "provisionCmd");
        this.cacheExpirationUrl = cacheExpirationUrl;
        this.cacheExpirationCmd = checkNotNullOrEmpty(cacheExpirationCmd, "cacheExpirationCmd");
        this.provisionedbootDelay = provisionedbootDelay;
        this.provisionStartTimeout = provisionStartTimeout;
        this.cmdProcessor = cmdProcessor;
        this.copySpec = copySpec;
    }

    private String checkNotNullOrEmpty(String arg, String argName) {
        checkArgument(arg != null && !arg.isEmpty(), "%s cannot be null or empty", argName);
        return arg;
    }

    @Override
    public void setup() {
        DomainWrapper cachedDomain = findFirstCachedDomain();
        if (cachedDomain == null) {
            logger.info("No cached domain, creating a new cached domain");

            // create a clone to provision
            super.setup();
            String ip = super.getHostName();

            provisionDomain(ip, copySpec, provisionStartTimeout);

            // shut down the provisioned domain so it can be cloned again
            DomainWrapper clone = super.getClone();
            clone.acpiShutdown();

            clone.updateMetadata(getBaseDomainName(), provisionCmd, getExpirationTag(), new Date());
            provisionedClone = createProvisionedClone();
        } else {
            String baseName = super.getBaseDomainName();
            String cloneName = baseName + "-" + UUID.randomUUID().toString();

            logger.info("Creating clone '{}' from cached domain '{}'", cloneName, cachedDomain.getName());
            provisionedClone = cachedDomain.cloneWithBackingStore(cloneName);
        }

        provisionedCloneIp = waitUntilRunningAndGetIP(provisionedClone);

        bootDelay(provisionedbootDelay);
    }

    protected void provisionDomain(String ip, List<String> copySpec, int startTimeout) {
        OverthereConnection remote = null;
        int seconds = startTimeout;
        String baseDomainName = this.getBaseDomainName();
        try {
            // SSH connections will fail in getRemoteConnection
            // CIFS in the actual provisioning call
            while (seconds >= 0) {
                try {
                    remote = getRemoteConnection(ip);
                    copyFiles(remote, copySpec);
                    provisionHost(remote, ip);
                    return;
                } catch (RuntimeIOException e) {
                    Throwable cause = e.getCause();
                    while (cause != null && !(cause instanceof ConnectException || cause instanceof NoRouteToHostException)) {
                        cause = cause.getCause();
                    }
                    if (!(cause instanceof ConnectException || cause instanceof NoRouteToHostException)) {
                        throw e;
                    }
                    logger.debug("Could not connect to '{}' at '{}' for provisioning, retrying", baseDomainName, ip);
                }
                sleep(1);
                seconds--;
            }
        } catch (RuntimeException e) {
            logger.error("Failed to provision '{}' cleaning up", baseDomainName);
            super.getClone().destroyWithDisks();
            throw e;
        } finally {
            if (remote != null) {
                remote.close();
            }
        }
        // timed out => clean up
        super.getClone().destroyWithDisks();
        throw new RuntimeException(String.format("Could not start provisioning clone from '%s' within %d seconds", baseDomainName, startTimeout));
    }

    protected DomainWrapper findFirstCachedDomain() {
        final String baseDomainName = super.getBaseDomainName();
        final String checkSum = getExpirationTag();
        logger.debug("Looking for a cached domain '{}' with checksum '{}'", baseDomainName, checkSum);
        try {
            List<Domain> domains = LibvirtUtil.getDefinedDomains(libvirt);
            for (Domain domain : domains) {
                String domainName = domain.getName();
                Document doc = LibvirtUtil.loadDomainXml(domain);
                Metadata md = Metadata.fromXml(doc);
                if (md == null || !md.isProvisioned()) {
                    continue;
                }
                logger.debug("Found domain '{}' with metadata {}", domainName, md);
                if (!md.getParentDomain().equals(baseDomainName)) {
                    continue;
                }
                if (!md.getProvisionedWith().equals(provisionCmd)) {
                    continue;
                }
                if (!md.getProvisionedChecksum().equals(checkSum)) {
                    logger.debug("Domain '{}' is stale (checksum={})", domainName, md.getProvisionedChecksum());
                    deleteStaleDomain(new DomainWrapper(domain, doc));
                    continue;
                }
                logger.debug("Found domain '{}' found for '{}'", domainName, baseDomainName);
                return new DomainWrapper(domain, doc);
            }
            logger.debug("No cached domain found for '{}' with checksum '{}'", baseDomainName, checkSum);
            return null;
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    protected void deleteStaleDomain(DomainWrapper staleDomain) throws LibvirtException {
        String staleDomainName = staleDomain.getName();

        if (isDomainSafeToDelete(libvirt, staleDomainName)) {
            try {
                logger.info("Destroying stale domain '{}'", staleDomainName);
                staleDomain.destroyWithDisks();
            } catch (LibvirtRuntimeException e) {
                // it may be that another job deleted the domain before us...
                logger.debug("Ignoring exception while cleaning stale domain", e);
            }
        }
    }

    protected static boolean isDomainSafeToDelete(Connect libvirt, String staleDomainName) throws LibvirtException {
        List<Domain> domains = LibvirtUtil.getRunningDomains(libvirt);

        for (Domain domain : domains) {
            Document doc = LibvirtUtil.loadDomainXml(domain);
            Metadata md = Metadata.fromXml(doc);
            if (md == null || md.isProvisioned()) {
                continue;
            }

            if (md.getParentDomain().equals(staleDomainName)) {
                logger.info("Not deleting stale domain '{}' still used by '{}'", staleDomainName, domain.getName());
                return false;
            }
        }
        return true;
    }

    @Override
    public DomainWrapper getClone() {
        return provisionedClone;
    }

    @Override
    public String getHostName() {
        return provisionedCloneIp;
    }

    @Override
    public void teardown() {
        if (provisionedClone != null) {
            provisionedClone.destroyWithDisks();
            provisionedClone = null;
        }
    }

    protected String getExpirationTag() {
        logger.info("Executing expiration tag command: {}", cacheExpirationCmd);
        if (cacheExpirationUrl == null) {
            return getLocalExpirationTag();
        } else {
            return getRemoteExpirationTag();
        }
    }

    private String getRemoteExpirationTag() {
        OverthereConnection connection = null;
        try {
            connection = overthereConnectionFromURI(cacheExpirationUrl);

            CapturingOverthereExecutionOutputHandler stdOutCapture = capturingHandler();
            CapturingOverthereExecutionOutputHandler stdErrCapture = capturingHandler();
            CmdLine cmd = new CmdLine();
            cmd.addRaw(cacheExpirationCmd);
            int exitCode = connection.execute(stdOutCapture, stdErrCapture, cmd);
            if (exitCode != 0) {
                throw new RuntimeException(String.format("Error getting expiration tag exit code %d, stdout=%s, stderr=%s", exitCode,
                    stdOutCapture.getOutput(), stdErrCapture.getOutput()));
            }
            return stdOutCapture.getOutput();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private String getLocalExpirationTag() {
        try {
            String expirationTag = cmdProcessor.run(Command.fromString(cacheExpirationCmd)).getOutput().trim();
            return expirationTag;
        } catch (NonZeroCodeException e) {
            throw new IllegalArgumentException(
                String.format(
                    "Command %s returned code %s with the following errors: \n\n%s\n",
                    e.getCommand().toString(),
                    e.getResponse().getReturnCode(),
                    e.getResponse().getErrors() + "\n\n" + e.getResponse().getOutput()
                    ));
        }
    }

    protected DomainWrapper createProvisionedClone() {
        DomainWrapper base = super.getClone();
        String baseName = super.getBaseDomainName();
        String cloneName = baseName + "-instance-" + UUID.randomUUID().toString();

        logger.info("Creating clone '{}' from provisioned domain '{}'", cloneName, base.getName());
        return base.cloneWithBackingStore(cloneName);
    }

    protected OverthereConnection getRemoteConnection(String ip) {
        String finalUrl = MessageFormat.format(provisionUrl, ip);
        return overthereConnectionFromURI(finalUrl);
    }

    protected void copyFiles(OverthereConnection remote, List<String> copySpec) {
        if (copySpec.isEmpty()) {
            return;
        }
        logger.info("Copying files into host: {}", copySpec);
        OverthereConnection local = LocalConnection.getLocalConnection();
        OverthereUtil.copyFiles(local, remote, copySpec);
    }

    protected void provisionHost(OverthereConnection remote, String ip) {
        CmdLine cmdLine = new CmdLine();
        String fragment = MessageFormat.format(provisionCmd, ip);
        cmdLine.addRaw(fragment);
        logger.info("Provisioning host with '{}'", cmdLine);

        CapturingOverthereExecutionOutputHandler stdOutCapture = capturingHandler();
        CapturingOverthereExecutionOutputHandler stdErrCapture = capturingHandler();

        OverthereExecutionOutputHandler stdOutHandler = stdOutCapture;
        OverthereExecutionOutputHandler stdErrHandler = stdErrCapture;

        if (logger.isInfoEnabled()) {
            OverthereExecutionOutputHandler stdout = new LoggingOutputHandler(logger, "out");
            stdOutHandler = multiHandler(stdOutCapture, stdout);

            OverthereExecutionOutputHandler stderr = new LoggingOutputHandler(logger, "err");
            stdErrHandler = multiHandler(stdErrCapture, stderr);
        }

        int exitCode = remote.execute(stdOutHandler, stdErrHandler, cmdLine);
        if (exitCode != 0) {
            throw new RuntimeException(String.format("Provisioning of clone from '%s' failed with exit code %d", getBaseDomainName(), exitCode));
        }

        // doesn't seem to work, we don't get stderr returned overthere/sshj bug?
        if (!stdErrCapture.getOutputLines().isEmpty()) {
            throw new RuntimeException(String.format("Provisioning of clone from '%s' failed with output to stderr: '%s'", getBaseDomainName(),
                stdErrCapture.getOutput()));
        }
    }
}
