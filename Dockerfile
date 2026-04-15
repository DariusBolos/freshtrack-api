# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache deps first
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
RUN ./mvnw -q -DskipTests dependency:go-offline

# Build
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Use the built JAR (assuming a single jar in target/)
COPY --from=build /app/target/*.jar /app/app.jar

# Render expects the app to listen on $PORT
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
