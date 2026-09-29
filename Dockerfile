# Estágio 1: compilação
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

# Estágio 2: execução
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/carteira-digital-api-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]