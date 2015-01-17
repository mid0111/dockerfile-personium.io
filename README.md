Dockerfile for personium.io
=======================

This repository contains Dockerfile of [personium.io](http://personium.io/) for Docker's automated build.

## Base Docker Image

* [tomcat:7.0.57-jre7](https://registry.hub.docker.com/u/library/tomcat/)

## Using Docker Image

* [maven](https://registry.hub.docker.com/_/maven/)
* [dockerfile / elasticsearch](https://registry.hub.docker.com/u/dockerfile/elasticsearch/)
* [memcached](https://registry.hub.docker.com/_/memcached/)

## Installation

### Linux or Mac OS X

1. Install [Docker](https://www.docker.com/).
2. Build docker image.

  ```bash
$ git clone git@github.com:mid0111/dockerfile-personium.io.git; cd dockerfile-personium.io; WORK_DIR=`pwd`
$ make
  ```


### Windows

1. Install [Docker](https://www.docker.com/).
2. Build war file.  
To reduce time for downloading required libraries, mount local directory.
If you want to build on clean environment every time, remove `-v ~/.m2:/root/.m2` option.  

  ````bash
$ git clone git@github.com:mid0111/dockerfile-personium.io.git; cd dockerfile-personium.io; WORK_DIR=`pwd`
$ git clone git@github.com:personium/io.git ${WORK_DIR}/resources/work/io
$ docker run -it --rm --name maven -v ${WORK_DIR}/resources/work/io/core:/usr/src/core -v  ~/.m2:/root/.m2  -w /usr/src/core maven mvn clean package
$ docker run -it --rm --name maven -v ${WORK_DIR}/resources/work/io/engine:/usr/src/engine -v ~/.m2:/root/.m2 -w /usr/src/engine maven mvn clean package
  ````
* Build personium docker image.

  ````bash
$ docker build -t dockerfile/personium .
  ````
* Build Elasticsearch-1.3.4 docker image.  
Because [dockerfile / elasticsearch](https://registry.hub.docker.com/u/dockerfile/elasticsearch/) does not support version tag now, you must build docker image for Elasticsearch 1.3.4.

  ```bash
$ git clone git@github.com:dockerfile/elasticsearch.git ${WORK_DIR}/resources/work/elasticsearch
$ sed -i -e 's/\(ENV ES_PKG_NAME elasticsearch-\).*/\11.3.4/g' ${WORK_DIR}/resources/work/elasticsearch/Dockerfile
$ echo -e '\n\naction:\n  auto_create_index: false' >> ${WORK_DIR}/resources/work/elasticsearch/config/elasticsearch.yml
$ docker build -t dockerfile/elasticsearch-1.3.4 ${WORK_DIR}/resources/work/elasticsearch
  ```

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
