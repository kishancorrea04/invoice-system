FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src /app/src
RUN mvn clean install -DskipTests=true
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/invoice-system-0.0.1-SNAPSHOT.jar /app/app.jar
RUN rm -rf /app/src /app/pom.xml /app/target
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]