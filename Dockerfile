FROM tomcat:7.0.57-jre7

Add ./resources/tomcat/setenv.sh /usr/local/tomcat/bin/
ADD ./resources/work/io/core/target/dc1-core.war /usr/local/tomcat/webapps/
ADD ./resources/work/io/engine/target/dc1-engine.war /usr/local/tomcat/webapps/

EXPOSE 8080
CMD ["catalina.sh", "run"]

