FROM eclipse-temurin:21-jdk

WORKDIR /

COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .

RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies

COPY src src
COPY config config

RUN ./gradlew --no-daemon build

ADD https://repo1.maven.org/maven2/io/sentry/sentry-opentelemetry-agent/8.43.1/sentry-opentelemetry-agent-8.43.1.jar sentry-opentelemetry-agent.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=50.0"
ENV SENTRY_AUTO_INIT=false
EXPOSE 8080

CMD ["sh", "-c", "java -javaagent:sentry-opentelemetry-agent.jar -jar $(ls build/libs/*.jar | grep -v -- '-plain\\.jar$' | head -n 1)"]
