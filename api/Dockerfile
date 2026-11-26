FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/copiloto-leroy-api.jar app.jar
ENV PORT=8080
EXPOSE 8080
# Render's free instance has 512 MB; leave headroom for the JVM's native memory.
CMD ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]
