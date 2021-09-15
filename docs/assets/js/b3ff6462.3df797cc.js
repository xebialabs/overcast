"use strict";(self.webpackChunkdocumentation=self.webpackChunkdocumentation||[]).push([[490],{2045:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return s},contentTitle:function(){return d},metadata:function(){return l},toc:function(){return m},default:function(){return u}});var i=n(7462),a=n(3366),o=(n(7294),n(3905)),r=["components"],s={sidebar_position:4},d="Libvirt",l={unversionedId:"getting-started/libvirt",id:"getting-started/libvirt",isDocsHomePage:!1,title:"Libvirt",description:"The libvirt implementation uses backing store images. This means that the domain being cloned needs to be shut down. When cloning a system all disks of the base system are cloned using a backing store, and thrown away upon teardown, thus leaving the original system unchanged.",source:"@site/docs/getting-started/libvirt.md",sourceDirName:"getting-started",slug:"/getting-started/libvirt",permalink:"/overcast/docs/getting-started/libvirt",tags:[],version:"current",sidebarPosition:4,frontMatter:{sidebar_position:4},sidebar:"tutorialSidebar",previous:{title:"Properties",permalink:"/overcast/docs/getting-started/properties"},next:{title:"Docker",permalink:"/overcast/docs/getting-started/docker"}},m=[{value:"NAT network",id:"nat-network",children:[]},{value:"Bridged network",id:"bridged-network",children:[]},{value:"Routed network",id:"routed-network",children:[]},{value:"File system mapping",id:"file-system-mapping",children:[]}],p={toc:m};function u(e){var t=e.components,n=(0,a.Z)(e,r);return(0,o.kt)("wrapper",(0,i.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"libvirt"},"Libvirt"),(0,o.kt)("p",null,"The libvirt implementation uses backing store images. This means that the domain being cloned needs to be shut down. When cloning a system all disks of the base system are cloned using a backing store, and thrown away upon teardown, thus leaving the original system unchanged."),(0,o.kt)("p",null,"Machines can use static IP's using ",(0,o.kt)("inlineCode",{parentName:"p"},"{host}.ipLookupStrategy=static"),". It is up to you that you do not\nstart more than one. It is also possible to use DHCP using ",(0,o.kt)("inlineCode",{parentName:"p"},"{host}.ipLookupStrategy=SSH"),".\nYou have to specify the name of the Virtual Network in libvirt or the name of the bridge the domain\nis connected to and a command to lookup the IP on the DHCP server giving the system it's IP address.\nThe IP can then be retrieved using the ",(0,o.kt)("inlineCode",{parentName:"p"},"getHostName()")," method on the ",(0,o.kt)("inlineCode",{parentName:"p"},"CloudHost"),"."),(0,o.kt)("h2",{id:"nat-network"},"NAT network"),(0,o.kt)("p",null,"Due to the way NAT works the machine would only be accessible from the Libvirt (KVM) host. Example settings for a (NAT) network named ",(0,o.kt)("inlineCode",{parentName:"p"},"my_nat_network"),":"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},'nat_host {\n    libvirtURL="qemu+ssh://user@linux-box/system"\n    libvirtBaseDomain="my_base_domain"\n    networkDeviceId="my_nat_network"\n    ipLookupStrategy="SSH"\n    SSH {\n        url="ssh://user@linux-box?os=UNIX&connectionType=SCP&privateKeyFile="${user.home}"/.ssh/id_rsa&passphrase=bigsecret"\n        command="""grep {0} /var/lib/libvirt/dnsmasq/my_nat_network.leases | cut -d " " -f 3"""\n        timeout=30\n    }\n}\n')),(0,o.kt)("h2",{id:"bridged-network"},"Bridged network"),(0,o.kt)("p",null,"Example settings for a host connected to a bridge named ",(0,o.kt)("inlineCode",{parentName:"p"},"br0"),". Assuming a DHCP server ",(0,o.kt)("inlineCode",{parentName:"p"},"dhcp-box"),":"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},'bridged_host {\n    libvirtURL="qemu+ssh://user@linux-box/system"\n    libvirtBaseDomain="my_base_domain"\n    networkDeviceId="br0"\n    ipLookupStrategy="SSH"\n    SSH {\n        url="ssh://dhcp-query-user@dhcp-box?os=UNIX&connectionType=SCP&privateKeyFile="${user.home}"/.ssh/id_rsa&passphrase=bigsecret"\n        command="""grep {0} /var/lib/dnsmasq/dnsmasq.leases | cut -d " " -f 3"""\n        timeout=30\n    }\n}\n')),(0,o.kt)("h2",{id:"routed-network"},"Routed network"),(0,o.kt)("p",null,"A routed network is similar to a bridged network with the difference that ",(0,o.kt)("inlineCode",{parentName:"p"},"networkDeviceId")," should be set to the name of the routed network."),(0,o.kt)("h2",{id:"file-system-mapping"},"File system mapping"),(0,o.kt)("p",null,"With entries in the ",(0,o.kt)("inlineCode",{parentName:"p"},"{host}.fsMapping")," section it is possible to mount directories from the host in the created domain. For instance like:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},'my-host {\n    ...\n    fsMapping {\n        vagrant { hostPath = ${itest.vagrantDir}"/itest/vagrant", readOnly = true }\n        data    { hostPath = ${itest.dataDir}, readOnly = true, accessMode = "mapped" }\n    }\n}\n')),(0,o.kt)("p",null,"These mappings can be mounted on the domain with fstab entries like:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"data            /data               9p    ro,trans=virtio\nvagrant         /vagrant            9p    ro,trans=virtio\n")),(0,o.kt)("p",null,"For details on the accessMode variants see: ",(0,o.kt)("a",{parentName:"p",href:"http://libvirt.org/formatdomain.html#elementsFilesystems"},"http://libvirt.org/formatdomain.html#elementsFilesystems"),"."),(0,o.kt)("p",null,"It may be necessary to add the 9p file system drivers to the initrd image of the base domain image."))}u.isMDXComponent=!0}}]);