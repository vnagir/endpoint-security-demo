FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY normalizer-service/pom.xml normalizer-service/
RUN mvn -f normalizer-service/pom.xml dependency:go-offline -B
COPY normalizer-service/src normalizer-service/src
RUN mvn -f normalizer-service/pom.xml package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/normalizer-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
