FROM maven:4.0.0-rc-4-eclipse-temurin-25 as builder
WORKDIR /app
COPY . .
RUN mvn -f ./pom.xml clean package

FROM eclipse-temurin:25 as runner
WORKDIR /app
COPY --from=builder --chown=app:app /app/target /app/target
ENV MODEL_HOST="http://127.0.0.1:8081"
EXPOSE 8080
CMD ["java", "-jar", "/app/target/frontend-0.0.1-SNAPSHOT.jar"]