FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY libs/xoLib.jar libs/xoLib.jar
EXPOSE 6000/TCP
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]