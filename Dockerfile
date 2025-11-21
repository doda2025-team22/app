FROM maven:4.0.0-rc-4-eclipse-temurin-25 AS builder
WORKDIR /app
COPY . .
RUN mvn -f ./pom.xml clean package

FROM eclipse-temurin:25 AS runner
WORKDIR /app
COPY --from=builder --chown=app:app /app/target /app/target
ENV MODEL_HOST="http://127.0.0.1:8081"

# Default backend port
ENV FRONTEND_PORT=8080
EXPOSE ${FRONTEND_PORT}

CMD ["java", "-jar", "/app/target/frontend-0.0.1-SNAPSHOT.jar", "--server.port=${FRONTEND_PORT}"]