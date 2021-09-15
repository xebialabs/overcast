---
sidebar_position: 5
---

# Docker

## Docker concepts
During `setup()`, Overcast will create and start a new Docker container. If the image specified is not available in the local registry, it will be automatically pulled from the central Docker repository.

During `teardown()`, it will stop the container and optionally remove the container (see remove property).

Calling `getHostName()` will return the hostname of the Docker Host, assuming the container will run on that host, with the exposed ports accessible on the Docker host.

Calling `getPort(port)` will translate the internal port (passed as an argument) to the port externally exposed by the Docker Container. The port number is dynamically determined by Docker. The port range used for dynamic allocation is 49153 to 65535 (defined by Docker).

We use the [Spotify Docker Client](https://github.com/spotify/docker-client) library.

