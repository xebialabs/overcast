---
sidebar_position: 1
---

# Installation

## Requirements

- Virtualbox version >= 4.2
- Vagrant version >= 1.2.7
- Qemu/KVM version that supports domain metadata (QEMU-KVM 1.4.2 (Fedora 19), 2.0.0 (Ubuntu LTS 14))
- Docker >= 1.6
- VMWare >= 7.0

## From maven repo

[http://mvnrepository.com/artifact/com.xebialabs.cloud/overcast/2.5.1](http://mvnrepository.com/artifact/com.xebialabs.cloud/overcast/2.5.1)

    <dependency>
        <groupId>com.xebialabs.cloud</groupId>
        <artifactId>overcast</artifactId>
        <version>2.5.1</version>
    </dependency>

Note: the libvirt JNA wrapper may require adding the libvirt.org repository to your build: [http://www.libvirt.org/maven2/](http://www.libvirt.org/maven2/)

## From sources

   ```gradle build```

## Usage

### Set up your host
Overcast looks for configuration properties in this order:

* `System.getProperty()`
* `<HOMEDIR>/.overcast/overcast.conf`
* `<WORKDIR>/overcast.conf`
* `<CLASSPATH>/overcast.conf`

:::note
The home location takes precedence over the project location.<br/>This allows developers to adapt settings to their local setup without changing the project defaults.
:::

The `overcast.conf` files are in [Typesafe Config HOCON syntax](https://github.com/typesafehub/config#using-hocon-the-json-superset).<br/>This is a flexible JSON superset that allows comments, substitution, file inclusion, and more.
