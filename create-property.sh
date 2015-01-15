#!/bin/sh

PERSONIUM_IO_ENV=`docker run -it --rm -p 8080:8080 --name personium --link elasticsearch:elasticsearch --link memcache:memcache dockerfile/personium env`

ES_HOST=`echo "${PERSONIUM_IO_ENV}" | grep ELASTICSEARCH_PORT_9300_TCP_ADDR | sed -e 's/.*=\(.*\)$/\1/'`
MEMCACHE_HOST=`echo "${PERSONIUM_IO_ENV}" | grep MEMCACHE_PORT_11211_TCP_ADDR | sed -e 's/.*=\(.*\)$/\1/'`

sed -e "s/=\${ELASTICSEARCH_PORT_9300_TCP_ADDR}/=${ES_HOST}/g" -e "s/=\${MEMCACHE_PORT_11211_TCP_ADDR}/=${MEMCACHE_HOST}/g" ./resources/dc-config.properties > ./resources/conf/dc-config.properties
