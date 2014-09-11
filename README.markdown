# Overcast

A Java library to test against hosts in the cloud.

### Features

* Decouple test and test machine setup.
* Setup and tear-down for
   - Amazon EC2 hosts (Automatic host creation/destroy)
   - Vagrant hosts (Set up to the running state, tear down to the initial state)
   - VirtualBox hosts (Load snapshot and start, power off)
   - Libvirt managed KVM hosts (Fast clones using backing store, provisioning)
   - Tunneled cloud hosts (Reaching target host via ssh tunnel)

* Provides hostname and port mapping of created host (@see Ec2CloudHost)
* Caching of provisioned hosts (vagrant and KVM) with expiration checks

### Requirements

- Virtualbox version >= 4.2
- Vagrant version >= 1.2.7
- Qemu/KVM version that supports domain metadata (QEMU-KVM 1.4.2 (Fedora 19), 2.0.0 (Ubuntu LTS 14))

### Usage

#### Set up your host
Overcast looks for configuration properties in this order:

1. `System.getProperty()`
* `<HOMEDIR>/.overcast/overcast.conf`
* `<WORKDIR>/overcast.conf`
* `<CLASSPATH>/overcast.conf`

**Note: The home location takes precedence over the project location.** This allows developers to adapt settings to their local setup without changing the project defaults.

The `overcast.conf` files are in [Typesafe Config HOCON syntax](https://github.com/typesafehub/config#using-hocon-the-json-superset); this is a flexible JSON superset that allows comments, substitution, file inclusion, and more.

##### Common properties
{my-host-label}.hostname - Hostname. If is not set, overcast will try to create host (For Amazon hosts).

##### Tunneled properties
{my-host-label}.tunnel.username - Tunnel username

{my-host-label}.tunnel.password - Tunnel password

{my-host-label}.tunnel.ports - Tunnel ports. Comma separated.

{my-host-label}.tunnel.setupTimeout - Attempt to set up the tunnel for this many seconds, default 0.

##### Amazon EC2 properties

{my-host-label}.amiId - [Amazon AMI id](https://aws.amazon.com/amis/). E.g.:ami-c1724eb5

{my-host-label}.amiInstanceType - [Instance type](http://aws.amazon.com/ec2/instance-types/). E.g.: m1.small

{my-host-label}.amiSecurityGroup - AMI security group

{my-host-label}.amiKeyName - AMI key name

{my-host-label}.amiBootSeconds - How many seconds max do you expect AMI to boot

{my-host-label}.aws.endpoint - [Endpoint URL] (http://aws.amazon.com/articles/3912)

{my-host-label}.aws.accessKey - Access key

{my-host-label}.aws.secretKey - Secret key

##### Vagrant host properties
{my-host-label}.vagrantDir - Directory with Vagrantfile

{my-host-label}.vagrantIp - IP address of the Vagrant host

{my-host-label}.vagrantVm - Name of the Vagrant host

{my-host-label}.vagrantOs - OS type of the Vagrant host (WINDOWS, UNIX)

{my-host-label}.vagrantSnapshotExpirationCmd - Command used to expire the snapshot image of the Vagrant host, it will be executed in the `vagrantDir`.

##### VirtualBox host properties
{my-host-label}.vboxUuid - UUID of the virtual machine

{my-host-label}.vboxSnapshotUuid - UUID  of the snapshot

{my-host-label}.vboxIp - IP address of the virtual machine

##### Libvirt host properties
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

#### Set up and Tear down

    @BeforeClass
    public static void doInitHost() {
       CloudHostFactory.getCloudHost("{my-host-label}").setup();
    }

    @AfterClass
    public static void doTeardownHost() {
       CloudHostFactory.getCloudHost("{my-host-label}").teardown();
    }

Also Overcast is used for integration tests of [Overthere](https://github.com/xebialabs/overthere).

### Installation


#### From maven repo

[http://mvnrepository.com/artifact/com.xebialabs.cloud/overcast/2.3.0](http://mvnrepository.com/artifact/com.xebialabs.cloud/overcast/2.3.0)

    <dependency>
        <groupId>com.xebialabs.cloud</groupId>
        <artifactId>overcast</artifactId>
        <version>2.3.0</version>
    </dependency>

#### From sources

   gradle build

### Notes for setting up test systems

#### Libvirt

The libvirt implementation uses backing store images. This means that the domain being cloned needs to be shut down. When cloning a system all disks of the base system are cloned using a backing store, and thrown away upon teardown, thus leaving the original system unchanged.

Machines can use static IP's using `{host}.ipLookupStrategy=static`. It is up to you that you do not
start more than one. It is also possible to use DHCP using `{host}.ipLookupStrategy=SSH`.
You have to specify the name of the Virtual Network in libvirt or the name of the bridge the domain
is connected to and a command to lookup the IP on the DHCP server giving the system it's IP address.
The IP can then be retrieved using the ```getHostName()``` method on the ```CloudHost```.

##### NAT network
Due to the way NAT works the machine would only be accessible from the Libvirt (KVM) host. Example settings for a (NAT) network named `my_nat_network`:

    nat_host {
        libvirtURL="qemu+ssh://user@linux-box/system"
        libvirtBaseDomain="my_base_domain"
        networkDeviceId="my_nat_network"
        ipLookupStrategy="SSH"
        SSH {
            url="ssh://user@linux-box?os=UNIX&connectionType=SCP&privateKeyFile="${user.home}"/.ssh/id_rsa&passphrase=bigsecret"
            command="""grep {0} /var/lib/libvirt/dnsmasq/my_nat_network.leases | cut -d " " -f 3"""
            timeout=30
        }
    }

##### Bridged network
Example settings for a host connected to a bridge named `br0`. Assuming a DHCP server `dhcp-box`:

    bridged_host {
        libvirtURL="qemu+ssh://user@linux-box/system"
        libvirtBaseDomain="my_base_domain"
        networkDeviceId="br0"
        ipLookupStrategy="SSH"
        SSH {
            url="ssh://dhcp-query-user@dhcp-box?os=UNIX&connectionType=SCP&privateKeyFile="${user.home}"/.ssh/id_rsa&passphrase=bigsecret"
            command="""grep {0} /var/lib/dnsmasq/dnsmasq.leases | cut -d " " -f 3"""
            timeout=30
        }
    }

##### Routed network

A routed network is similar to a bridged network with the difference that `networkDeviceId` should be set to the name of the routed network.

##### File system mapping

With entries in the `{host}.fsMapping` section it is possible to mount directories from the host in the created domain. For instance like:

    my-host {
        ...
        fsMapping {
            vagrant { hostPath = ${itest.vagrantDir}"/itest/vagrant", readOnly = true }
            data    { hostPath = ${itest.dataDir}, readOnly = true, accessMode = "mapped" }
        }
    }

These mappings can be mounted on the domain with fstab entries like:

    data            /data               9p    ro,trans=virtio
    vagrant         /vagrant            9p    ro,trans=virtio

For details on the accessMode variants see: [http://libvirt.org/formatdomain.html#elementsFilesystems](http://libvirt.org/formatdomain.html#elementsFilesystems).

It may be necessary to add the 9p file system drivers to the initrd image of the base domain image.
