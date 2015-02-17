FROM tomcat:7.0.57-jre7

# Customize tomcat settings.
ADD ./resources/tomcat/setenv.sh /usr/local/tomcat/bin/

# Deploy war file.
ADD ./resources/work/io/core/target/dc1-core.war /usr/local/tomcat/webapps/
ADD ./resources/work/io/engine/target/dc1-engine.war /usr/local/tomcat/webapps/
ADD ./resources/conf/dc-config.properties /usr/local/personium/

EXPOSE 8080
CMD ["catalina.sh", "run"]

