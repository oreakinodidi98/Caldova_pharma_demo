FROM maven:3.9.9-eclipse-temurin-8 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:8-jre
LABEL com.azure.containerizationassist.createdby="containerization-assist"
WORKDIR /app
COPY --from=build /workspace/target/production-demo-1.0.0.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=6s --start-period=30s --retries=6 \
    CMD curl -fsS --max-time 5 http://127.0.0.1:8080/api/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
