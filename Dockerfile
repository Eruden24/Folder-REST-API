# Stage 1: build the jar with Maven
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
# Dependencies first, so a code-only change reuses the cached download
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -q
COPY src src
# Tests use Testcontainers, which needs Docker; run them on the host with ./mvnw verify
RUN ./mvnw package -DskipTests -q

# Stage 2: run the jar on a smaller JRE-only image
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
