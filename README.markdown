# Overcast

A Java library to test against hosts in the cloud.

### Features

* Setup and tear-down for
   - Amazon EC2 hosts (Automatic host creation/destroy)
   - Vagrant hosts (Set up to the running state, tear down to the initial state)
   - VirtualBox hosts (Load snapshot and start, power off)
   - Libvirt managed KVM hosts (Fast clone using backing store, only bridged networking supported)
   - Tunneled cloud hosts (Reaching target host via ssh tunnel)

* Provides hostname and port mapping of created host (@see Ec2CloudHost)

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

{my-host-label}.vagrantSnapshotExpirationCmd - Command used to expire the snapshot image of the Vagrant host

##### VirtualBox host properties
{my-host-label}.vboxUuid - UUID of the virtual machine

{my-host-label}.vboxSnapshotUuid - UUID  of the snapshot

{my-host-label}.vboxIp - IP address of the virtual machine


##### Libvirt host properties
{my-host-label}.libvirtURL - URL of libvirt e.g. qemu+ssh://user@linux-box/system

{my-host-label}.libvirtBaseDomain - name of the domain to clone

{my-host-label}.networkDeviceId - name of the network device that should be used for IP to MAC lookup. For example `br0`.

{my-host-label}.ipLookupStrategy - name of a strategy used to figure out the IP of the clone, static or SSH.

{my-host-label}.static.ip - When `ipLookupStrategy` is static, the static IP the created host is expected to have.

{my-host-label}.SSH.url - URL for overthere to connect to the system that knows about the MAC to IP mapping. For instance: `ssh://user@edhcpserver?os=UNIX&connectionType=SFTP&privateKeyFile=/home/user/.ssh/id_rsa&passphrase=bigsecret`

{my-host-label}.SSH.command - Command to execute on the system to lookup the IP. For example for dnsmasq: ```grep {0} /var/lib/misc/dnsmasq.leases | cut -d " " -f 3```. {0} is expanded to the MAC address.

{my-host-label}.SSH.timeout - Number of seconds to try the above command to find the IP.

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

[http://mvnrepository.com/artifact/com.xebialabs.cloud/overcast/1.3.0](http://mvnrepository.com/artifact/com.xebialabs.cloud/overcast/1.3.0)

    <dependency>
        <groupId>com.xebialabs.cloud</groupId>
        <artifactId>overcast</artifactId>
        <version>1.3.0</version>
    </dependency>

#### From sources

   gradle build

### Notes for setting up test systems

#### Libvirt

The libvirt implementation uses backing store images. This means that the domain being cloned needs to be shut down. When cloning a system all disks of the base system are cloned using a backing store, and thrown away upon teardown, thus leaving the original system unchanged.

Machines can use static IP's using `{host}.ipLookupStrategy=static`. It is up to you that you do not start more than one. It is also possible to use DHCP using `{host}.ipLookupStrategy=SSH`. You have to specify the name of the Virtual Network in libvirt or the name of the bridge the domain is connected to and a command to lookup the IP on the DHCP server giving the system it's IP address. The IP can then be retrieved using the ```getHostName()``` method on the ```CloudHost```.

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
