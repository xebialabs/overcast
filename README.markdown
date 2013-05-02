# Overcast

A Java library to test against hosts in the cloud.

### Features

* Setup and tear-down for
	- Amazon EC2 hosts (Automatic host creation/destroy)
	- Vagrant hosts (Set up to the running state, tear down to the initial state)
	- VirtualBox hosts (Load snapshot and start, poweroff)
	- Tunneled cloud hosts (Reaching target host via ssh tunnel)

* Provides hostname and port mapping of created host (@see Ec2CloudHost)

### Usage

#### Setup your host
There are 2 places where Overcast tries to find properties for configuration (higher position in list - higher precedence):

* System.getProperty()
* ~/.overcast/overcast.properties
* src/test/resources/overcast.properties

**Home location takes precedence on project location.**

##### Common properties
{my-host-label}.hostname - Hostname. If is not set, overthere will try to create host (For Amazon hosts).

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


##### VirtualBox host properties
{my-host-label}.vboxUuid - UUID of the virtual machine

{my-host-label}.vboxSnapshotUuid - UUID  of the snapshot

{my-host-label}.vboxIp - IP address of the virtual machine


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

[http://mvnrepository.com/artifact/com.xebialabs.cloud/overcast/1.1.1](http://mvnrepository.com/artifact/com.xebialabs.cloud/overcast/1.1.1)

	<dependency>
    	<groupId>com.xebialabs.cloud</groupId>
    	<artifactId>overcast</artifactId>
    	<version>1.1.1</version>
    </dependency>

#### From sources

	gradle build