"use strict";(self.webpackChunkdocumentation=self.webpackChunkdocumentation||[]).push([[241],{1336:function(t,e,a){a.r(e),a.d(e,{frontMatter:function(){return l},contentTitle:function(){return p},metadata:function(){return m},toc:function(){return s},default:function(){return c}});var r=a(7462),n=a(3366),o=(a(7294),a(3905)),i=["components"],l={sidebar_position:3},p="Properties",m={unversionedId:"getting-started/properties",id:"getting-started/properties",isDocsHomePage:!1,title:"Properties",description:"Common properties",source:"@site/docs/getting-started/properties.md",sourceDirName:"getting-started",slug:"/getting-started/properties",permalink:"/overcast/docs/getting-started/properties",tags:[],version:"current",sidebarPosition:3,frontMatter:{sidebar_position:3},sidebar:"tutorialSidebar",previous:{title:"Features",permalink:"/overcast/docs/getting-started/features"},next:{title:"Libvirt",permalink:"/overcast/docs/getting-started/libvirt"}},s=[{value:"Common properties",id:"common-properties",children:[]},{value:"Tunneled properties",id:"tunneled-properties",children:[]},{value:"Amazon EC2 properties",id:"amazon-ec2-properties",children:[]},{value:"Vagrant host properties",id:"vagrant-host-properties",children:[]},{value:"VirtualBox host properties",id:"virtualbox-host-properties",children:[]},{value:"Libvirt host properties",id:"libvirt-host-properties",children:[]},{value:"Docker container properties",id:"docker-container-properties",children:[]},{value:"VM Ware properties",id:"vm-ware-properties",children:[]}],d={toc:s};function c(t){var e=t.components,a=(0,n.Z)(t,i);return(0,o.kt)("wrapper",(0,r.Z)({},d,a,{components:e,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"properties"},"Properties"),(0,o.kt)("h2",{id:"common-properties"},"Common properties"),(0,o.kt)("table",null,(0,o.kt)("thead",{parentName:"table"},(0,o.kt)("tr",{parentName:"thead"},(0,o.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,o.kt)("tbody",{parentName:"table"},(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.hostname"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Hostname. If is not set, overcast will try to create host (For Amazon hosts).")))),(0,o.kt)("h2",{id:"tunneled-properties"},"Tunneled properties"),(0,o.kt)("table",null,(0,o.kt)("thead",{parentName:"table"},(0,o.kt)("tr",{parentName:"thead"},(0,o.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,o.kt)("tbody",{parentName:"table"},(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.tunnel.username"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Tunnel username")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.tunnel.password"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Tunnel password")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.tunnel.ports"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Tunnel ports. Comma separated.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.tunnel.setupTimeout"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Attempt to set up the tunnel for this many seconds, default 0.")))),(0,o.kt)("h2",{id:"amazon-ec2-properties"},"Amazon EC2 properties"),(0,o.kt)("table",null,(0,o.kt)("thead",{parentName:"table"},(0,o.kt)("tr",{parentName:"thead"},(0,o.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,o.kt)("tbody",{parentName:"table"},(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.amiId"),(0,o.kt)("td",{parentName:"tr",align:"center"},(0,o.kt)("a",{parentName:"td",href:"https://aws.amazon.com/amis/"},"Amazon AMI id"),". E.g.:ami-c1724eb5")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.amiInstanceType"),(0,o.kt)("td",{parentName:"tr",align:"center"},(0,o.kt)("a",{parentName:"td",href:"http://aws.amazon.com/ec2/instance-types/"},"Instance type"),". E.g.: m1.small")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.amiSecurityGroup"),(0,o.kt)("td",{parentName:"tr",align:"center"},"AMI security group")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.amiKeyName"),(0,o.kt)("td",{parentName:"tr",align:"center"},"AMI key name")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.amiBootSeconds"),(0,o.kt)("td",{parentName:"tr",align:"center"},"How many seconds max do you expect AMI to boot")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.aws.endpoint"),(0,o.kt)("td",{parentName:"tr",align:"center"},"[Endpoint URL]"," (",(0,o.kt)("a",{parentName:"td",href:"http://aws.amazon.com/articles/3912"},"http://aws.amazon.com/articles/3912"),")")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.aws.accessKey"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Access key")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.aws.secretKey"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Secret key")))),(0,o.kt)("h2",{id:"vagrant-host-properties"},"Vagrant host properties"),(0,o.kt)("table",null,(0,o.kt)("thead",{parentName:"table"},(0,o.kt)("tr",{parentName:"thead"},(0,o.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,o.kt)("tbody",{parentName:"table"},(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vagrantDir"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Directory with Vagrantfile")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vagrantIp"),(0,o.kt)("td",{parentName:"tr",align:"center"},"IP address of the Vagrant host")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vagrantParameters"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Additional parameters to pass for vagrant up command.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vagrantVm"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Name of the Vagrant host")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vagrantOs"),(0,o.kt)("td",{parentName:"tr",align:"center"},"OS type of the Vagrant host (WINDOWS, UNIX)")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vagrantSnapshotExpirationCmd"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Command used to expire the snapshot image of the Vagrant host, it will be executed in the ",(0,o.kt)("inlineCode",{parentName:"td"},"vagrantDir"),".")))),(0,o.kt)("p",null,"For example, to pass additional parameters for vagrant up command, something like ",(0,o.kt)("inlineCode",{parentName:"p"},"vagrant --param1=value1 --param2=value2 up")),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},'my-host-label {\n    vagrantParameters {\n        param1="value1"\n        param2="value2"\n    }\n}\n')),(0,o.kt)("h2",{id:"virtualbox-host-properties"},"VirtualBox host properties"),(0,o.kt)("table",null,(0,o.kt)("thead",{parentName:"table"},(0,o.kt)("tr",{parentName:"thead"},(0,o.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,o.kt)("tbody",{parentName:"table"},(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vboxUuid"),(0,o.kt)("td",{parentName:"tr",align:"center"},"UUID of the virtual machine")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vboxSnapshotUuid"),(0,o.kt)("td",{parentName:"tr",align:"center"},"UUID  of the snapshot")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vboxBoxIp"),(0,o.kt)("td",{parentName:"tr",align:"center"},"IP address of the virtual machine")))),(0,o.kt)("h2",{id:"libvirt-host-properties"},"Libvirt host properties"),(0,o.kt)("table",null,(0,o.kt)("thead",{parentName:"table"},(0,o.kt)("tr",{parentName:"thead"},(0,o.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,o.kt)("tbody",{parentName:"table"},(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.libvirtURL"),(0,o.kt)("td",{parentName:"tr",align:"center"},"URL of libvirt e.g. qemu+ssh://user@linux-box/system")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.libvirtStartTimeout"),(0,o.kt)("td",{parentName:"tr",align:"center"},"The libvirt domain must go into running state before this timeout (default: 30)")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.baseDomain"),(0,o.kt)("td",{parentName:"tr",align:"center"},"name of the domain to clone")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.network"),(0,o.kt)("td",{parentName:"tr",align:"center"},"name of the network device that should be used for IP to MAC lookup. For example ",(0,o.kt)("inlineCode",{parentName:"td"},"br0"),".")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.ipLookupStrategy"),(0,o.kt)("td",{parentName:"tr",align:"center"},"name of a strategy used to figure out the IP of the clone, static or SSH.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.static.ip"),(0,o.kt)("td",{parentName:"tr",align:"center"},"When ",(0,o.kt)("inlineCode",{parentName:"td"},"ipLookupStrategy")," is static, the static IP the created host is expected to have.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.SSH.url"),(0,o.kt)("td",{parentName:"tr",align:"center"},"URL for overthere to connect to the system that knows about the MAC to IP mapping. For instance: ",(0,o.kt)("inlineCode",{parentName:"td"},"ssh://user@dhcpserver?os=UNIX&connectionType=SFTP&privateKeyFile=/home/user/.ssh/id_rsa&passphrase=bigsecret"))),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.SSH.command"),(0,o.kt)("td",{parentName:"tr",align:"center"},"command to execute on the system to lookup the IP. For example for dnsmasq: ```grep {0} /var/lib/misc/dnsmasq.leases")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.SSH.timeout"),(0,o.kt)("td",{parentName:"tr",align:"center"},"number of seconds to try the above command to find the IP.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.provision.url"),(0,o.kt)("td",{parentName:"tr",align:"center"},"URL for overthere to connect to the created system. For instance: ",(0,o.kt)("inlineCode",{parentName:"td"},'ssh://user@{0}?os=UNIX&connectionType=SCP&privateKeyFile="${user.home}"/.ssh/id_rsa&passphrase=bigsecret'),". {0} will be replaced by the IP the system got.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.provision.bootDelay"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Boot delay to use after the system has been provisioned.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.provision.startTimeout"),(0,o.kt)("td",{parentName:"tr",align:"center"},"After the boot delay the copy and provision commands will be retried until this timeout expires. This allows for more robust startup when startup times vary, without configuring a big bootDelay.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.provision.copy"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Files/directories specified here will be copied to the system before provisioning. If the length of the list is even then files/directories are copied pair wise. If the length is odd then everything is copied into the last entry (which should be a directory).")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.provision.cmd"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Command to run to provision the system.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.provision.expirationTag.cmd"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Command to run to determine the expiration tag of a cached provisioning operation. Runs locally in the current directory. If an overthere URL is specified it will run the command remotely.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.provision.expirationTag.url"),(0,o.kt)("td",{parentName:"tr",align:"center"},"URL for overthere to connect to a machine hosting the scripts that were used to provision an image.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.fsMapping.{target}.hostPath"),(0,o.kt)("td",{parentName:"tr",align:"center"},"upon cloning create a Filesystem mapping between hostPath and ",(0,o.kt)("inlineCode",{parentName:"td"},"target")," in the host.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.fsMapping.{target}.accessMode"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Access mode, one of passthrough, mapped, squash (default: passthrough)")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.fsMapping.{target}.readOnly"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Whether the mount will be readOnly (default: true)")))),(0,o.kt)("h2",{id:"docker-container-properties"},"Docker container properties"),(0,o.kt)("table",null,(0,o.kt)("thead",{parentName:"table"},(0,o.kt)("tr",{parentName:"thead"},(0,o.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,o.kt)("tbody",{parentName:"table"},(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.dockerHost"),(0,o.kt)("td",{parentName:"tr",align:"center"},"The hostname of the Docker Host. (default: ",(0,o.kt)("inlineCode",{parentName:"td"},"http://localhost:2375"),"). It can also be a unix socket: ",(0,o.kt)("inlineCode",{parentName:"td"},"unix:///var/run/docker.sock"),".")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.certificates"),(0,o.kt)("td",{parentName:"tr",align:"center"},"The certificates to use when connecting to a HTTPS secured docker host. The directory must contain ",(0,o.kt)("inlineCode",{parentName:"td"},"ca.pem"),", ",(0,o.kt)("inlineCode",{parentName:"td"},"cert.pem")," and ",(0,o.kt)("inlineCode",{parentName:"td"},"key.pem"),".")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.dockerImage"),(0,o.kt)("td",{parentName:"tr",align:"center"},"The Docker image that will be run. (required)")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.name"),(0,o.kt)("td",{parentName:"tr",align:"center"},"The name the container will get. Warning: Docker container names must be unique, even when the container is stopped. Use in combination with ",(0,o.kt)("inlineCode",{parentName:"td"},"remove")," to make sure you can start a container with the same name again. Also not suitable for parallel testing. (default: random name defined by docker)")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.remove"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Boolean. If true, the container will be removed during teardown. (default: false)")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.exposedPorts"),(0,o.kt)("td",{parentName:"tr",align:"center"},"List of ports to expose. Use in combination with ",(0,o.kt)("inlineCode",{parentName:"td"},"exposeAllPorts"),". Must include the protocol. Currently only ",(0,o.kt)("inlineCode",{parentName:"td"},"tcp")," is supported. For example: ",(0,o.kt)("inlineCode",{parentName:"td"},'["12345/tcp", "23456/tcp"]'),".")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.exposeAllPorts"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Boolean. If true, Docker will expose the ports defined by the Docker image (see ",(0,o.kt)("a",{parentName:"td",href:"https://docs.docker.com/reference/builder/#expose"},"EXPOSE"),"), and additionally the ports defined in overcast property ",(0,o.kt)("inlineCode",{parentName:"td"},"exposedPorts"),". (default: false)")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.command"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Command to execute within the container. For example: ",(0,o.kt)("inlineCode",{parentName:"td"},'["/bin/sh", "-c", "while true; do echo hello world; sleep 1; done"]'))),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.env"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Environment variables that will be exported in the container. For example: ",(0,o.kt)("inlineCode",{parentName:"td"},'["MYVAR1=AAA", "MYVAR2=BBB", "MYVAR3=CCC"]'),".")))),(0,o.kt)("h2",{id:"vm-ware-properties"},"VM Ware properties"),(0,o.kt)("table",null,(0,o.kt)("thead",{parentName:"table"},(0,o.kt)("tr",{parentName:"thead"},(0,o.kt)("th",{parentName:"tr",align:"center"},"Name"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Default Value"),(0,o.kt)("th",{parentName:"tr",align:"center"},"Description"))),(0,o.kt)("tbody",{parentName:"table"},(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.ignoreBadCertificate"),(0,o.kt)("td",{parentName:"tr",align:"center"},"false"),(0,o.kt)("td",{parentName:"tr",align:"center"},"This property can be useful during transitional period when on VM host not installed a valid certificate.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.instanceClone"),(0,o.kt)("td",{parentName:"tr",align:"center"},"true"),(0,o.kt)("td",{parentName:"tr",align:"center"},"What type of clone to do. It is possible to do usual")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.maxRetries"),(0,o.kt)("td",{parentName:"tr",align:"center"},"15"),(0,o.kt)("td",{parentName:"tr",align:"center"},"How many times to retry in case of failure. For example when VM is spinning up and trying to get the IP address from it.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.securityAlgorithm"),(0,o.kt)("td",{parentName:"tr",align:"center"},"TLS"),(0,o.kt)("td",{parentName:"tr",align:"center"},"It matters only when ignoreBadCertificate=true")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vmBaseImage"),(0,o.kt)("td",{parentName:"tr",align:"center"},"None"),(0,o.kt)("td",{parentName:"tr",align:"center"},"The name of the image which will be cloned. The cloned image will have name as {base-imaage-name}-{8 alpha numeric characters}.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vmwareApiHost"),(0,o.kt)("td",{parentName:"tr",align:"center"},"None"),(0,o.kt)("td",{parentName:"tr",align:"center"},"The host where VMWare is running.")),(0,o.kt)("tr",{parentName:"tbody"},(0,o.kt)("td",{parentName:"tr",align:"center"},"{my-host-label}.vmwareStartTimeout"),(0,o.kt)("td",{parentName:"tr",align:"center"},"180"),(0,o.kt)("td",{parentName:"tr",align:"center"},"Specified in seconds. It's a timeout for a REST client how long to wait for a reply.")))))}c.isMDXComponent=!0}}]);