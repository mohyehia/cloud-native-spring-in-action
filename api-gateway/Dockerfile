FROM eclipse-temurin:21-jre AS builder
WORKDIR /workspace
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} api-gateway.jar
RUN java -Djarmode=layertools -jar api-gateway.jar extract

FROM eclipse-temurin:21-jre
RUN useradd spring
USER spring
WORKDIR /workspace
COPY --from=builder workspace/dependencies/ ./
COPY --from=builder workspace/spring-boot-loader/ ./
COPY --from=builder workspace/snapshot-dependencies/ ./
COPY --from=builder workspace/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
