---
sidebar_position: 4
---

# Libvirt

The libvirt implementation uses backing store images. This means that the domain being cloned needs to be shut down. When cloning a system all disks of the base system are cloned using a backing store, and thrown away upon teardown, thus leaving the original system unchanged.

Machines can use static IP's using `{host}.ipLookupStrategy=static`. It is up to you that you do not
start more than one. It is also possible to use DHCP using `{host}.ipLookupStrategy=SSH`.
You have to specify the name of the Virtual Network in libvirt or the name of the bridge the domain
is connected to and a command to lookup the IP on the DHCP server giving the system it's IP address.
The IP can then be retrieved using the ```getHostName()``` method on the ```CloudHost```.

## NAT network
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

## Bridged network
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

## Routed network

A routed network is similar to a bridged network with the difference that `networkDeviceId` should be set to the name of the routed network.

## File system mapping

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
