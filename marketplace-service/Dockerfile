FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties ./


RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

COPY src/ src/
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
