FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY pom.xml .

RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

COPY . .

RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=0 /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

