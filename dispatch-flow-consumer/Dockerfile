# Build consumer from multi-module project
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY guides-shared ./guides-shared
COPY dispatch-flow-consumer ./dispatch-flow-consumer
COPY producer ./producer
RUN ./mvnw -pl dispatch-flow-consumer -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/dispatch-flow-consumer/target/dispatch-flow-consumer-0.0.1-SNAPSHOT.jar /app/app.jar
COPY wallet/ /app/wallet/
ENV TNS_ADMIN=/app/wallet
EXPOSE 8081
VOLUME /app/efs
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
