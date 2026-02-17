FROM eclipse-temurin:17-jre
WORKDIR /app
# Копируем уже готовый jar-файл из твоей папки target
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]