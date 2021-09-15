---
sidebar_position: 2
---

# Features

* Decouple test and test machine setup.
* Setup and tear-down for
   - Amazon EC2 hosts (Automatic host creation/destroy)
   - Vagrant hosts (Set up to the running state, tear down to the initial state)
   - VirtualBox hosts (Load snapshot and start, power off)
   - Libvirt managed KVM hosts (Fast clones using backing store, provisioning)
   - Docker containers
   - Tunneled cloud hosts (Reaching target host via ssh tunnel)

* Provides hostname and port mapping of created host (@see Ec2CloudHost)
* Caching of provisioned hosts (vagrant and KVM) with expiration checks
