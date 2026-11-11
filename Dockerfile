# eclipse-temurin publishes linux/amd64 and linux/arm64 (Oracle Cloud Ampere).
# openjdk:21-alpine is deprecated and is not a maintained base image.

FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx512m -XX:MaxMetaspaceSize=256m -Dorg.gradle.daemon=false -Dorg.gradle.vfs.watch=false"

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src src

RUN chmod +x gradlew \
    && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app/logs /app/secrets

COPY --from=build /workspace/build/libs/jamye-0.0.1-SNAPSHOT.jar /app/app.jar

# Heap stays inside the container limit so MySQL and Redis still have RAM.
# Override JAVA_OPTS in .env on a larger VM.
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=55.0 -XX:InitialRAMPercentage=25.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul -Djava.security.egd=file:/dev/urandom" \
    SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
