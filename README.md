Dockerfile for personium.io
=======================

This repository contains Dockerfile of personium.io for Docker's automated build.

## Base Docker Image

* [tomcat:7.0.57-jre7](https://registry.hub.docker.com/u/library/tomcat/)

## Using Docker Image

* [maven](https://registry.hub.docker.com/_/maven/)
* [dockerfile / elasticsearch](https://registry.hub.docker.com/u/dockerfile/elasticsearch/)

## Installation

1. Install [Docker](https://www.docker.com/).
2. Build war file.  
To reduce time for downloading required libraries, mount local directory.
If you want to build on clean environment every time, remove `-v ${WORK_DIR}/resources/.m2:/root/.m2` option.  

  ````bash
$ WORK_DIR={your working direcroty}
$ cd ${WORK_DIR}
$ mkdir -p resources/work; cd $_
$ git clone git@github.com:personium/io.git
$ docker run -it --rm --name maven -v ${WORK_DIR}/resources/work/io/core:/usr/src/core -v  ${WORK_DIR}/resources/.m2:/root/.m2  -w /usr/src/core maven mvn clean package
$ docker run -it --rm --name maven -v ${WORK_DIR}/resources/work/io/engine:/usr/src/engine -v ${WORK_DIR}/resources/.m2:/root/.m2 -w /usr/src/engine maven mvn clean package
  ````
* Build docker image.

  ````bash
$ cd ${WORK_DIR}
$ docker build -t dockerfile/personium .
  ````

## Usage

1. Start Elasticsearch deamon.  

  ````bash
$ docker run -d -p 9200:9200 -p 9300:9300 --name elasticsearch dockerfile/elasticsearch
  ````
* Start personium.io.  
To connect Elasticsearch docker container, describe ip address on `dc-config.properties`.

  ````bash
$ ES_HOST=`docker run -it --rm -p 8080:8080 --name personium --link elasticsearch:elasticsearch dockerfile/personium env | grep ELASTICSEARCH_PORT_9300_TCP_ADDR | sed -e 's/.*=\(.*\)$/\1/'`
$ sed -e "s/=\${ELASTICSEARCH_PORT_9300_TCP_ADDR}/=${ES_HOST}/g" ./resources/dc-config.properties > ./resources/conf/dc-config.properties
$ docker run -it --rm -p 8080:8080 --name personium -v ${WORK_DIR}/resources/conf:/usr/local/personium --link elasticsearch:elasticsearch dockerfile/personium
  ````
  