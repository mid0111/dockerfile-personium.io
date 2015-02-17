WORK_DIR=$(shell pwd)
PERSONIUM_DIR=${WORK_DIR}/resources/work/io
ES_DIR=${WORK_DIR}/resources/work/elasticsearch

ES_ID=`sudo docker images -q elasticsearch-1.3.4`
PERSONIUM_ID=`sudo docker images -q personium`

docker: war
	if [ ! -z $(PERSONIUM_ID) ] ; then sudo docker rmi $(PERSONIUM_ID) ; fi
	sudo docker build -t personium ${WORK_DIR}

war: elasticsearch
	if [ ! -d ${PERSONIUM_DIR} ]; then git clone https://github.com/personium/io.git ${PERSONIUM_DIR}; fi
	docker run -it --rm --name maven -v ${PERSONIUM_DIR}/core:/usr/src/core -v  ~/.m2:/root/.m2  -w /usr/src/core maven mvn clean package
	docker run -it --rm --name maven -v ${PERSONIUM_DIR}/engine:/usr/src/engine -v ~/.m2:/root/.m2 -w /usr/src/engine maven mvn clean package

elasticsearch: 
	echo ${ES_ID}
	echo ${PERSONIUM_ID}
	echo "-------"
	if [ ! -d ${ES_DIR} ]; then git clone https://github.com/dockerfile/elasticsearch.git ${ES_DIR}; fi
	sed -i -e 's/\(ENV ES_PKG_NAME elasticsearch-\).*/\11.3.4/g' ${ES_DIR}/Dockerfile
	if [ -z `grep 'auto_create_index: false' ${ES_DIR}/config/elasticsearch.yml` ]; then echo '\n\naction:\n  auto_create_index: false' >> ${ES_DIR}/config/elasticsearch.yml; fi
	if [ -z $(ES_ID) ] ; then sudo docker rmi $(ES_ID) ; fi
	sudo docker build -t elasticsearch-1.3.4 ${ES_DIR}

