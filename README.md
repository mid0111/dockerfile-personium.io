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

1. Start Elasticsearch deamon.  

  ````bash
$ docker run -d -p 9200:9200 -p 9300:9300 --name elasticsearch dockerfile/elasticsearch-1.3.4
  ````
* Start memcached demon.

  ````bash
$ docker run --name memcache -d memcached
  ````
* Start personium.io.  
To connect Elasticsearch and memcached docker container, describe ip address on `dc-config.properties`.

  ````bash
$ sh create-property.sh 
$ docker run -it --rm -p 8080:8080 --name personium -v `pwd`/resources/conf:/usr/local/personium --link elasticsearch:elasticsearch --link memcache:memcache dockerfile/personium
  ````

## Installation

1. Install [Docker](https://www.docker.com/).
2. Build docker image.

  ```bash
$ git clone git@github.com:mid0111/dockerfile-personium.io.git; cd dockerfile-personium.io
$ make
  ```
