FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/ipt-backend.jar
COPY ${JAR_FILE} ipt-backend.jar
ENTRYPOINT ["java", "-jar", "/ipt-backend.jar"]