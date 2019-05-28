FROM java:8
VOLUME /tmp
EXPOSE 8080
ADD target/SpringBootRest.jar SpringBootRest.jar
ENTRYPOINT ["java","-jar","SpringBootRest.jar"]