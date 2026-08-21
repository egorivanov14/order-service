FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/*SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]