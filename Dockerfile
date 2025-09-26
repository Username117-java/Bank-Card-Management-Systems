FROM eclipse-temurin:17-jdk-jammy
ARG JAR_FILE=target/bank-rest-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]