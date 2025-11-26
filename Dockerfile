FROM eclipse-temurin:25
WORKDIR /app

# Build arguments to pull jar file from GitHub Packages
ARG GH_ACTOR
ARG GH_TOKEN
ARG VERSION

RUN curl -u "${GH_ACTOR}:${GH_TOKEN}" -L \
    "https://maven.pkg.github.com/doda2025-team22/app/com/doda/app/${VERSION}/app-${VERSION}.jar" \
    -o app.jar

# Default Model host
ENV MODEL_HOST="http://127.0.0.1:8081"

# Default backend port
ENV FRONTEND_PORT=8080
EXPOSE ${FRONTEND_PORT}

CMD ["java", "-jar", "/app/app.jar", "--server.port=${FRONTEND_PORT}"]