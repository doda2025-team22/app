FROM ubuntu:jammy AS downloader
WORKDIR /app

# Build arguments to pull jar file from GitHub Packages
ARG GH_ACTOR
ARG GH_TOKEN
ARG VERSION

RUN apt-get update && apt-get install -y curl

RUN curl -u "${GH_ACTOR}:${GH_TOKEN}" -L\
    "https://maven.pkg.github.com/doda2025-team22/app/com/doda/app/app/${VERSION}/app-${VERSION}.jar"\
    -o app.jar

FROM eclipse-temurin:25 AS runner
WORKDIR /app
COPY --from=downloader /app/app.jar /app/app.jar
# Default Model host
ENV MODEL_HOST="http://127.0.0.1:8081"

# Default backend port
ENV FRONTEND_PORT=8080
EXPOSE ${FRONTEND_PORT}

CMD ["java", "-jar", "/app/app.jar", "--server.port=${FRONTEND_PORT}"]