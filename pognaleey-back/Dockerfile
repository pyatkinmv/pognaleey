# Этап 1: Сборка приложения
FROM maven:3.9-eclipse-temurin-21-alpine AS build
# Указываем рабочую директорию внутри контейнера
WORKDIR /app
# Копируем файл конфигурации Maven и зависимости
COPY pom.xml ./
COPY src ./src
# Запускаем сборку Maven
RUN mvn clean install -DskipTests

# Этап 2: Запуск приложения
FROM eclipse-temurin:21-jre-alpine
# Устанавливаем активный профиль Spring
ENV SPRING_PROFILES_ACTIVE=prod
# Включаем Java Debug Wire Protocol (JDWP) для отладки
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
# Копируем собранный JAR из первого этапа
COPY --from=build /app/target/pognaleey-*.jar /usr/app/pognaleey.jar
# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "/usr/app/pognaleey.jar"]