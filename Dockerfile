FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
# Las pruebas usan H2 aislado; no se necesitan secretos ni una BD de produccion.
RUN mvn -B -ntp clean verify

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build --chown=app:app /build/target/ApiSpringCafeSoft-0.0.1-SNAPSHOT.jar /app/app.jar
USER app
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
