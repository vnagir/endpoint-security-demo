FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY analytics-service/pom.xml analytics-service/
RUN mvn -f analytics-service/pom.xml dependency:go-offline -B
COPY analytics-service/src analytics-service/src
RUN mvn -f analytics-service/pom.xml package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/analytics-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
