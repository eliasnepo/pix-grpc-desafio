FROM openjdk:11-jdk-slim
ARG JAR_FILE=build/libs/*-all.jar
ADD ${JAR_FILE} app.jar
EXPOSE 50051
ENV APP_NAME keymanager-grpc
ENTRYPOINT [ "java", "-jar", "/app.jar" ]