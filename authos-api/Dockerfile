FROM eclipse-temurin:17-jre-alpine
LABEL authors="stevetosak"
WORKDIR /app
COPY ./target/Authos-0.0.1-alpha.jar .
ENTRYPOINT ["java", "-Xms64m","-Xmx256m","-XX:+UseSerialGC","-Dspring.profiles.active=prod","-jar", "Authos-0.0.1-alpha.jar"]