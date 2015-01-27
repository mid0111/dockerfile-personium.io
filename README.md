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
$ docker run -d -p 9200:9200 -p 9300:9300 --name elasticsearch elasticsearch-1.3.4
  ````
* Start memcached demon.

  ````bash
$ docker run --name memcache -d memcached
  ````
* Start personium.io.  
To connect Elasticsearch and memcached docker container, describe ip address on `dc-config.properties`.

  ````bash
$ docker run -d -p 8080:8080 --name personium -v `pwd`/resources/conf:/usr/local/personium --link elasticsearch:elasticsearch --link memcache:memcache personium
  ````

## Installation

### Linux or Mac OS X

1. Install [Docker](https://www.docker.com/).
2. Build docker image.

  ```bash
$ git clone git@github.com:mid0111/dockerfile-personium.io.git; cd dockerfile-personium.io
$ make
  ```

### Windows

1. Install [Docker](https://www.docker.com/).
2. Try below in docker shell.
  ````bash
# Build war file.
git clone https://github.com/mid0111/dockerfile-personium.io.git; cd dockerfile-personium.io; WORK_DIR=`pwd`
git clone https://github.com/personium/io.git ${WORK_DIR}/resources/work/io
docker run -it --rm --name maven -v ${WORK_DIR}/resources/work/io/core:/usr/src/core -v  ${WORK_DIR}/resources/.m2:/root/.m2  -w /usr/src/core maven mvn clean package
docker run -it --rm --name maven -v ${WORK_DIR}/resources/work/io/engine:/usr/src/engine -v ${WORK_DIR}/resources/.m2:/root/.m2 -w /usr/src/engine maven mvn clean package

# Build personium docker image.
docker build -t personium .

# Build Elasticsearch-1.3.4 docker image.
git clone https://github.com/dockerfile/elasticsearch.git ${WORK_DIR}/resources/work/elasticsearch
sed -i -e 's/\(ENV ES_PKG_NAME elasticsearch-\).*/\11.3.4/g' ${WORK_DIR}/resources/work/elasticsearch/Dockerfile
echo -e '\n\naction:\n  auto_create_index: false' >> ${WORK_DIR}/resources/work/elasticsearch/config/elasticsearch.yml
docker build -t elasticsearch-1.3.4 ${WORK_DIR}/resources/work/elasticsearch
  ```
