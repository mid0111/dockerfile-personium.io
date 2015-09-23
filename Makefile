WORK_DIR=$(shell pwd)
PERSONIUM_DIR=${WORK_DIR}/resources/work/io

PERSONIUM_ID=`docker images -q personium`

docker: war
	if [ ! -z $(PERSONIUM_ID) ] ; then docker rmi $(PERSONIUM_ID) ; fi
	docker build -t personium ${WORK_DIR}

war:
	if [ ! -d ${PERSONIUM_DIR} ]; then git clone https://github.com/personium/io.git ${PERSONIUM_DIR};	fi
	@which mvn > /dev/null \
	&& (mvn package -f ${PERSONIUM_DIR}/core/pom.xml; mvn package -f ${PERSONIUM_DIR}/engine/pom.xml) \
	|| (docker run -it --rm --name maven -v ${PERSONIUM_DIR}/core:/usr/src/core -v  ~/.m2:/root/.m2  -w /usr/src/core maven mvn clean package; docker run -it --rm --name maven -v ${PERSONIUM_DIR}/engine:/usr/src/engine -v ~/.m2:/root/.m2 -w /usr/src/engine maven mvn clean package)

.PHONY: docker war

