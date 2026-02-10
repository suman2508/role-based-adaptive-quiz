# RUNBOOK — AI Role-Based Adaptive Quiz & Preparation Platform (Backend)

Operational guide to run the backend locally and in Docker. Includes environment setup, commands, health checks, and troubleshooting.

## 1) Prerequisites

- Java 17+ (verify: `java -version`)
- Maven 3.9+ (verify: `mvn -v`)
- Docker + Docker Compose (verify: `docker -v`, `docker compose version`)
- Internet access to Maven Central + Spring Milestone repo

## 2) Start Infrastructure (PostgreSQL + Redis)

From the project root:

- docker compose up -d

Services provisioned:
- Postgres 16 at localhost:5432
  - DB: quizdb, User: quiz, Password: quiz (see docker-compose.yml)
- Redis 7 at localhost:6379 (optional; used for caching later)

Check health:
- docker ps
- Postgres healthcheck: `pg_isready -U quiz -d quizdb` (container executes this periodically)

To stop:
- docker compose down

## 3) Application Configuration (Environment Variables)

Defaults exist in application.yml. Override with env vars as needed.

Database:
- SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quizdb
- SPRING_DATASOURCE_USERNAME=quiz
- SPRING_DATASOURCE_PASSWORD=quiz

AI (optional until you use AI endpoints):
- SPRING_AI_OPENAI_API_KEY=<your-key>
- SPRING_AI_OPENAI_BASE_URL=<optional-compatible-endpoint>

JWT (to be used once auth endpoints are added):
- APP_JWT_SECRET=<strong-secret>
- APP_JWT_EXPIRATION=3600000

Redis (optional):
- SPRING_DATA_REDIS_HOST=localhost
- SPRING_DATA_REDIS_PORT=6379

Windows (CMD):
- set SPRING_AI_OPENAI_API_KEY=<your-key>

Windows (PowerShell):
- $env:SPRING_AI_OPENAI_API_KEY="<your-key>"

Linux/macOS (bash/zsh):
- export SPRING_AI_OPENAI_API_KEY=<your-key>

## 4) Build

Dev build (with dev profile):
- mvn clean package -Pdev

If you need to skip tests during fast iterations:
- mvn clean package -Pdev -DskipTests

Annotation processing notes:
- Lombok + MapStruct require annotation processing enabled in IDE.
- If types are “unresolved” in IDE, run: mvn -DskipTests=true compile and reimport Maven project.

## 5) Run (Local Dev)

Option A: Maven
- mvn spring-boot:run -Dspring-boot.run.profiles=dev

Option B: Executable JAR
- java -jar target/hackathon-0.0.1-SNAPSHOT.jar

Default port: 8080 (override with SERVER_PORT or server.port)

Flyway:
- Runs automatically at startup, applying migrations under classpath:db/migration

## 6) Run (Dockerizing the App)

Build image:
- docker build -t quiz-app:latest .

Run container (connects to host Postgres on Windows/macOS via host.docker.internal):
- docker run --rm -p 8080:8080 ^
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/quizdb ^
  -e SPRING_DATASOURCE_USERNAME=quiz ^
  -e SPRING_DATASOURCE_PASSWORD=quiz ^
  -e SPRING_AI_OPENAI_API_KEY=<your-key-optional> quiz-app:latest

Linux alternatives:
- Use bridge network and pass proper host IP (e.g., 172.17.0.1) or run with `--network=host` (if available on your distro).

## 7) Health & Documentation

- Health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 8) Postman Collection

Import:
- postman/AdaptiveQuiz.postman_collection.json

Environment:
- Set `baseUrl = http://localhost:8080`
- Adjust `userId`, `roleId`, `questionId` as needed

Included requests:
- Roles:
  - POST /roles/analyze
  - GET /roles/{roleId}/skills
- Roadmap (placeholder)
  - POST /roadmap/generate/{userId}
  - GET /roadmap/{userId}
- Quiz (placeholder)
  - POST /quiz/start
  - POST /quiz/submit
  - GET /quiz/next-adaptive
- Performance (placeholder)
  - GET /performance/{userId}
  - GET /performance/readiness-score/{userId}
- Schedule (placeholder)
  - GET /schedule/{userId}

## 9) Profiles

Configured in application.yml:
- dev: verbose SQL and bind parameter logs
- prod: reduced logs and hardened actuator exposure

Pick profile:
- Maven run: `-Dspring-boot.run.profiles=dev`
- JAR run: `--spring.profiles.active=dev`

## 10) Troubleshooting

Spring AI dependency resolution:
- pom.xml uses Spring AI 1.0.0-M3 and the Spring Milestones repo.
- If resolution fails, run `mvn -U clean package` to update indexes.

Lombok/MapStruct “cannot be resolved”:
- Ensure IDE annotation processing is enabled.
- Reimport Maven project.
- Run `mvn -DskipTests=true compile`.

Postgres connection failures:
- Confirm container is running: `docker ps`
- Check logs: `docker logs quiz-postgres`
- Verify credentials and SPRING_DATASOURCE_* env vars.

Port conflicts:
- Change mapped ports in docker-compose.yml or set `SERVER_PORT` for the app.

Flyway migration errors:
- Ensure DB is reachable; drop/recreate volumes if schema is inconsistent:
  - `docker compose down -v` (removes data volumes; DEV ONLY!)
  - `docker compose up -d`

Redis unreachable (optional):
- Ensure `quiz-redis` container is up; verify port 6379 not occupied.

## 11) Security (JWT/RBAC)

- SecurityConfig is scaffolded for stateless JWT with roles USER/ADMIN.
- Auth endpoints (/auth/register, /auth/login) are to be added.
- Configure `APP_JWT_SECRET` and rotate in production.

## 12) Production Notes

- Externalize secrets via environment or secret managers.
- Enable HTTPS/Reverse proxy in front (e.g., Nginx).
- Add observability (metrics, logs, traces).
- Scale DB and add read-replicas if needed.
- Optionally enable Redis caching and pgvector (add migration for embeddings).

## 13) Make It Your Own

- Extend the Postman collection as endpoints are completed.
- Add Testcontainers-based integration tests for Flyway migrations.
- Implement the adaptive quiz rule engine under `service/rules` and add unit tests.
