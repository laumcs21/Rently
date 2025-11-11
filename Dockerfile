# ===== stage 1: build =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# copiamos lo mínimo primero para aprovechar cache
COPY mvnw pom.xml ./
COPY .mvn .mvn

# descarga dependencias
RUN ./mvnw -q -DskipTests dependency:go-offline

# ahora sí copiamos el código
COPY src src

# construimos el jar
RUN ./mvnw -DskipTests package

# ===== stage 2: runtime =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# copia el jar generado
COPY --from=build /app/target/*.jar app.jar

# puerto del spring
EXPOSE 8080

# que apunte al contenedor mysql llamado "db"
ENV SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/rently
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=0108lomejor

# healthcheck (curl)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health || exit 1

# usuario no root
USER 1000

ENTRYPOINT ["java","-jar","app.jar"]
