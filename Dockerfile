FROM eclipse-temurin:21-jdk

WORKDIR /

COPY / .

RUN ./gradlew --no-daemon clean build

EXPOSE 8080

CMD ["java", "-jar", "build/libs/java-project-99-0.0.1-SNAPSHOT.jar"]
