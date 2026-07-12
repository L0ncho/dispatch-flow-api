# Build producer (API) from multi-module project
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY guides-shared ./guides-shared
COPY producer ./producer
COPY dispatch-flow-consumer ./dispatch-flow-consumer
RUN ./mvnw -pl producer -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/producer/target/dispatch-flow-api-0.0.1-SNAPSHOT.jar /app/app.jar
COPY wallet/ /app/wallet/
ENV TNS_ADMIN=/app/wallet
EXPOSE 8080
VOLUME /app/efs
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
