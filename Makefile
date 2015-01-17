WORK_DIR=$(shell pwd)
PERSONIUM_DIR=${WORK_DIR}/resources/work/io
ES_DIR=${WORK_DIR}/resources/work/elasticsearch

ES_ID=`docker images -q dockerfile/elasticsearch-1.3.4`
PERSONIUM_ID=`docker images -q dockerfile/personium`

docker: war
	if [ -z $(PERSONIUM_ID) ] ; then docker rmi $(PERSONIUM_ID) ; fi
	docker build -t dockerfile/personium ${WORK_DIR}

war: elasticsearch
	if [ ! -d ${PERSONIUM_DIR} ]; then git clone git@github.com:personium/io.git ${PERSONIUM_DIR}; fi
	docker run -it --rm --name maven -v ${PERSONIUM_DIR}/core:/usr/src/core -v  ~/.m2:/root/.m2  -w /usr/src/core maven mvn clean package
	docker run -it --rm --name maven -v ${PERSONIUM_DIR}/engine:/usr/src/engine -v ~/.m2:/root/.m2 -w /usr/src/engine maven mvn clean package

elasticsearch: 
	if [ ! -d ${ES_DIR} ]; then git clone git@github.com:dockerfile/elasticsearch.git ${ES_DIR}; fi
	sed -i -e 's/\(ENV ES_PKG_NAME elasticsearch-\).*/\11.3.4/g' ${ES_DIR}/Dockerfile
	echo '\n\naction:\n  auto_create_index: false' >> ${ES_DIR}/config/elasticsearch.yml
	if [ -z $(ES_ID) ] ; then docker rmi $(ES_ID) ; fi
	docker build -t dockerfile/elasticsearch-1.3.4 ${ES_DIR}
