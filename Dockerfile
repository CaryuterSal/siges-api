# ---------- BUILD STAGE ----------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon -x test

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75","-jar","app.jar"]