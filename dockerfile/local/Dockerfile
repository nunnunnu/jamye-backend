FROM eclipse-temurin:21-jdk

WORKDIR /usr/src/app

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

COPY ./build/libs/*.jar ./

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=local", "jamye-0.0.1-SNAPSHOT.jar"]