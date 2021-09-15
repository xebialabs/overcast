---
sidebar_position: 3
---

# Properties

## Common properties
{my-host-label}.hostname - Hostname. If is not set, overcast will try to create host (For Amazon hosts).

## Tunneled properties
{my-host-label}.tunnel.username - Tunnel username

{my-host-label}.tunnel.password - Tunnel password

{my-host-label}.tunnel.ports - Tunnel ports. Comma separated.

{my-host-label}.tunnel.setupTimeout - Attempt to set up the tunnel for this many seconds, default 0.

## Amazon EC2 properties

{my-host-label}.amiId - [Amazon AMI id](https://aws.amazon.com/amis/). E.g.:ami-c1724eb5

{my-host-label}.amiInstanceType - [Instance type](http://aws.amazon.com/ec2/instance-types/). E.g.: m1.small

{my-host-label}.amiSecurityGroup - AMI security group

{my-host-label}.amiKeyName - AMI key name

{my-host-label}.amiBootSeconds - How many seconds max do you expect AMI to boot

{my-host-label}.aws.endpoint - [Endpoint URL] (http://aws.amazon.com/articles/3912)

{my-host-label}.aws.accessKey - Access key

{my-host-label}.aws.secretKey - Secret key

## Vagrant host properties
{my-host-label}.vagrantDir - Directory with Vagrantfile

{my-host-label}.vagrantIp - IP address of the Vagrant host

{my-host-label}.vagrantVm - Name of the Vagrant host

{my-host-label}.vagrantOs - OS type of the Vagrant host (WINDOWS, UNIX)

{my-host-label}.vagrantSnapshotExpirationCmd - Command used to expire the snapshot image of the Vagrant host, it will be executed in the `vagrantDir`.

## VirtualBox host properties
{my-host-label}.vboxUuid - UUID of the virtual machine

{my-host-label}.vboxSnapshotUuid - UUID  of the snapshot

{my-host-label}.vboxBoxIp - IP address of the virtual machine

## Libvirt host properties
{my-host-label}.libvirtURL - URL of libvirt e.g. qemu+ssh://user@linux-box/system

{my-host-label}.libvirtStartTimeout - The libvirt domain must go into running state before this timeout (default: 30)

{my-host-label}.baseDomain - name of the domain to clone

{my-host-label}.network - name of the network device that should be used for IP to MAC lookup. For example `br0`.

{my-host-label}.ipLookupStrategy - name of a strategy used to figure out the IP of the clone, static or SSH.

{my-host-label}.static.ip - When `ipLookupStrategy` is static, the static IP the created host is expected to have.

{my-host-label}.SSH.url - URL for overthere to connect to the system that knows about the MAC to IP mapping. For instance: `ssh://user@dhcpserver?os=UNIX&connectionType=SFTP&privateKeyFile=/home/user/.ssh/id_rsa&passphrase=bigsecret`

{my-host-label}.SSH.command - command to execute on the system to lookup the IP. For example for dnsmasq: ```grep {0} /var/lib/misc/dnsmasq.leases | cut -d " " -f 3```. {0} is expanded to the MAC address.

{my-host-label}.SSH.timeout - number of seconds to try the above command to find the IP.

{my-host-label}.provision.url - URL for overthere to connect to the created system. For instance: `ssh://user@{0}?os=UNIX&connectionType=SCP&privateKeyFile="${user.home}"/.ssh/id_rsa&passphrase=bigsecret`. {0} will be replaced by the IP the system got.

{my-host-label}.provision.bootDelay - Boot delay to use after the system has been provisioned.

{my-host-label}.provision.startTimeout - After the boot delay the copy and provision commands will be retried until this timeout expires. This allows for more robust startup when startup times vary, without configuring a big bootDelay.

{my-host-label}.provision.copy - Files/directories specified here will be copied to the system before provisioning. If the length of the list is even then files/directories are copied pair wise. If the length is odd then everything is copied into the last entry (which should be a directory).

{my-host-label}.provision.cmd - Command to run to provision the system.

{my-host-label}.provision.expirationTag.cmd - Command to run to determine the expiration tag of a cached provisioning operation. Runs locally in the current directory. If an overthere URL is specified it will run the command remotely.

{my-host-label}.provision.expirationTag.url - URL for overthere to connect to a machine hosting the scripts that were used to provision an image.

{my-host-label}.fsMapping.{target}.hostPath - upon cloning create a Filesystem mapping between hostPath and `target` in the host.

{my-host-label}.fsMapping.{target}.accessMode - Access mode, one of passthrough, mapped, squash (default: passthrough)

{my-host-label}.fsMapping.{target}.readOnly - Whether the mount will be readOnly (default: true)

#### Docker concepts
During `setup()`, Overcast will create and start a new Docker container. If the image specified is not available in the local registry, it will be automatically pulled from the central Docker repository.

During `teardown()`, it will stop the container and optionally remove the container (see remove property).

Calling `getHostName()` will return the hostname of the Docker Host, assuming the container will run on that host, with the exposed ports accessible on the Docker host.

Calling `getPort(port)` will translate the internal port (passed as an argument) to the port externally exposed by the Docker Container. The port number is dynamically determined by Docker. The port range used for dynamic allocation is 49153 to 65535 (defined by Docker).

We use the [Spotify Docker Client](https://github.com/spotify/docker-client) library.

## Docker container properties
{my-host-label}.dockerHost - The hostname of the Docker Host. (default: `http://localhost:2375`). It can also be a unix socket: `unix:///var/run/docker.sock`.

{my-host-label}.certificates - The certificates to use when connecting to a HTTPS secured docker host. The directory must contain `ca.pem`, `cert.pem` and `key.pem`.

{my-host-label}.dockerImage - The Docker image that will be run. (required)

{my-host-label}.name - The name the container will get. Warning: Docker container names must be unique, even when the container is stopped. Use in combination with `remove` to make sure you can start a container with the same name again. Also not suitable for parallel testing. (default: random name defined by docker)

{my-host-label}.remove - Boolean. If true, the container will be removed during teardown. (default: false)

{my-host-label}.exposedPorts - List of ports to expose. Use in combination with `exposeAllPorts`. Must include the protocol. Currently only `tcp` is supported. For example: `["12345/tcp", "23456/tcp"]`.

{my-host-label}.exposeAllPorts - Boolean. If true, Docker will expose the ports defined by the Docker image (see [EXPOSE](https://docs.docker.com/reference/builder/#expose)), and additionally the ports defined in overcast property `exposedPorts`. (default: false)

{my-host-label}.command - Command to execute within the container. For example: `["/bin/sh", "-c", "while true; do echo hello world; sleep 1; done"]`

{my-host-label}.env - Environment variables that will be exported in the container. For example: `["MYVAR1=AAA", "MYVAR2=BBB", "MYVAR3=CCC"]`.
