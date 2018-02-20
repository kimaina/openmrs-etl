FROM tomcat:7

ADD openmrs-runtime.properties /usr/local/tomcat/openmrs-runtime.properties
ADD setenv.sh /usr/local/tomcat/bin/setenv.sh

RUN apt-get update && apt-get install -y curl unzip; \
    curl -L http://sourceforge.net/projects/openmrs/files/releases/OpenMRS_Platform_1.11.5/openmrs.war/download \
         -o /usr/local/tomcat/webapps/openmrs.war; \
    mkdir -p /usr/local/tomcat/.OpenMRS/modules; \
    curl -L http://sourceforge.net/projects/openmrs/files/releases/OpenMRS_2.3.1/openmrs-2.3.1-modules.zip/download \
         -o /tmp/openmrs-2.3.1-modules.zip; \
    unzip /tmp/openmrs-2.3.1-modules.zip -d /tmp; \
    mv /tmp/openmrs-2.3.1-modules/*.omod /usr/local/tomcat/.OpenMRS/modules; \
    rm -rf /tmp/openmrs-2.3.1-modules*;
