FROM openjdk:11
COPY target/demo-0.0.1-SNAPSHOT*.jar /usr/src/demo-0.0.1-SNAPSHOT.jar
COPY src/main/resources/application.properties /opt/conf/application.properties
CMD ["java", "-jar", "/home/noaa/Desktop/crawlerTask/target/demo-0.0.1-SNAPSHOT.jar", "--spring.config.location=file:/opt/conf/application.properties"]