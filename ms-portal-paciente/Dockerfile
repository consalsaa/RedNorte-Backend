FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Descarga las dependencias primero (mejora el cache de Docker)
RUN mvn dependency:go-offline -B
COPY src ./src
# Compila saltando los tests para hacerlo más rápido
RUN mvn clean package -DskipTests

# Segunda etapa: imagen ligera solo con el JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
