Dockerfile for personium.io
=======================

This repository contains Dockerfile of [personium.io](http://personium.io/) for Docker's automated build.  

## Base Docker Image

* [tomcat:7.0.57-jre7](https://registry.hub.docker.com/u/library/tomcat/)

## Using Docker Image

* [maven](https://registry.hub.docker.com/_/maven/)
* [dockerfile / elasticsearch](https://registry.hub.docker.com/u/dockerfile/elasticsearch/)
* [memcached](https://registry.hub.docker.com/_/memcached/)

## Usage

See the [Docker Hub page](https://registry.hub.docker.com/u/mid0111/personium.io/) for the full readme on how to use the Docker image.


## Manual installation

1. Install [Docker](https://www.docker.com/).
2. Build docker image.

  ```bash
$ git clone git@github.com:mid0111/dockerfile-personium.io.git; cd dockerfile-personium.io
$ make
  ```
