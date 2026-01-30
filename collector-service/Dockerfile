FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY collector-service/pom.xml collector-service/
RUN mvn -f collector-service/pom.xml dependency:go-offline -B
COPY collector-service/src collector-service/src
RUN mvn -f collector-service/pom.xml package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/collector-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
