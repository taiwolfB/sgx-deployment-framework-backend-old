FROM maven:3.8.3-openjdk-17 AS builder

COPY ./src /var/backend/src
# COPY src/main/resources/sensor.csv /var/lib/backend/
COPY ./pom.xml /var/backend
WORKDIR /var/backend
RUN mvn package -Dmaven.test.skip=true
RUN java -Djarmode=layertools -jar /var/backend/target/sgx-deployment-framework-backend-0.0.1-SNAPSHOT.jar list
RUN java -Djarmode=layertools -jar /var/backend/target/sgx-deployment-framework-backend-0.0.1-SNAPSHOT.jar extract
RUN ls -l /var/backend

FROM openjdk:17-jdk-slim

COPY --from=builder /var/backend/dependencies/ ./
COPY --from=builder /var/backend/snapshot-dependencies/ ./

RUN sleep 10
COPY --from=builder /var/backend/spring-boot-loader/ ./
COPY --from=builder /var/backend/application/ ./
EXPOSE 8082
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher","-XX:+UseContainerSupport -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -Xms512m -Xmx512m -XX:+UseG1GC -XX:+UseSerialGC -Xss512k -XX:MaxRAM=72m"]
