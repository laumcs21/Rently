# ===== stage 1: build =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn

# ✅ Agregar permisos
RUN chmod +x mvnw

RUN ./mvnw -q -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -DskipTests package

# ===== stage 2: runtime =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health || exit 1

USER 1000
ENTRYPOINT ["java","-jar","app.jar"]
