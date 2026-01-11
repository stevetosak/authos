FROM eclipse-temurin:17-jre-alpine
LABEL authors="stevetosak"
WORKDIR /app
COPY ./target/Authos-1.0.0-alpha.jar .
RUN mkdir -p /etc/keystore
ENTRYPOINT ["java", "-Xms64m","-Xmx256m","-XX:+UseSerialGC","-Dspring.profiles.active=prod","-jar", "Authos-1.0.0-alpha.jar"]