FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/InventoryManagementSystem-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 5050
# MaxRAMPercentage lets the heap use the container limit instead of the JVM's
# conservative default fraction of host RAM.
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-XX:+UseG1GC","-jar","app.jar"]