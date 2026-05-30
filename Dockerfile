FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew && ./gradlew --no-daemon clean build

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/java-project-99-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]
