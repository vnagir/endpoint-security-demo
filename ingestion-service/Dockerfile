FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY ingestion-service/pom.xml ingestion-service/
RUN mvn -f ingestion-service/pom.xml dependency:go-offline -B
COPY ingestion-service/src ingestion-service/src
RUN mvn -f ingestion-service/pom.xml package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/ingestion-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
