FROM openjdk:21-slim
LABEL authors="GEOGREEN-PLATFORM"
COPY target/keycloak-auth-service*.jar /keycloak-auth-service.jar
ENTRYPOINT ["java", "-jar", "/keycloak-auth-service.jar"]