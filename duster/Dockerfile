# syntax=docker/dockerfile:1

# ---- Stage 1: warm the Gradle dependency cache ----
FROM gradle:8.13-jdk21 AS cache
ENV GRADLE_USER_HOME=/home/gradle/cache_home
WORKDIR /home/gradle/app
COPY build.gradle.* gradle.properties settings.gradle.* ./
COPY gradle ./gradle
RUN gradle --no-daemon dependencies || true

# ---- Stage 2: build the fat jar ----
FROM gradle:8.13-jdk21 AS build
ENV GRADLE_USER_HOME=/home/gradle/cache_home
COPY --from=cache /home/gradle/cache_home /home/gradle/cache_home
WORKDIR /home/gradle/app
COPY . .
RUN gradle --no-daemon buildFatJar

# ---- Stage 3: runtime ----
FROM eclipse-temurin:21-jre AS runtime
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --uid 10001 --home /app duster
WORKDIR /app
# `fat.jar` is the exact name set in build.gradle.kts (ktor.fatJar.archiveFileName).
COPY --from=build /home/gradle/app/build/libs/fat.jar /app/duster-client.jar
# Numeric UID — a non-numeric USER can't be verified against Kubernetes `runAsNonRoot`.
USER 10001
# Duster honours PORT (default 8785); EXPOSE is documentation only.
EXPOSE 8785
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -fsS "http://localhost:${PORT:-8785}/health/live" || exit 1
ENTRYPOINT ["java","-jar","/app/duster-client.jar"]
