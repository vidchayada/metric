# Girlette — Spring Boot + Vue re-engineering (SE262)

| Layer | Original | Re-engineered |
|---|---|---|
| UI | Bootstrap + custom CSS | Tailwind CSS |
| Frontend | EJS (server-rendered) | Vue 3 + TypeScript |
| Backend | Express.js | Spring Boot |
| Database | MySQL | PostgreSQL |

```
metric/
├── backend/                 Spring Boot (Maven)
├── frontend/                Vue 3 + Vite
├── pom.xml                  root aggregator: builds backend, runs frontend tests, drives SonarQube
├── docker-compose.yml       PostgreSQL (+ app, profile "app")
├── docker-compose.sonar.yml local SonarQube
└── .github/workflows/ci.yml
```

## Run locally
```bash
cp .env.example .env            # then edit the passwords
docker compose up -d            # PostgreSQL
cd backend  && ./mvnw spring-boot:run
cd frontend && npm install && npm run dev      # http://localhost:5173
```

## SonarQube (Maven)
```bash
docker compose -f docker-compose.sonar.yml up -d     # http://localhost:9000
# create a project token in SonarQube (My Account > Security), then from the repo root:
mvn -B verify sonar:sonar -Dsonar.token=<token>
```
One command runs backend tests (JaCoCo), frontend tests (Vitest coverage) and uploads both.
Skip the frontend step with `-Dskip.npm -Dskip.installnodenpm`.

### Required in `backend/pom.xml`
The root pom does not configure Java coverage. Add JaCoCo to the backend `<build><plugins>`:
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.12</version>
  <executions>
    <execution><goals><goal>prepare-agent</goal></goals></execution>
    <execution><id>report</id><phase>verify</phase><goals><goal>report</goal></goals></execution>
  </executions>
</plugin>
```

### GitHub Actions
Add repository secrets `SONAR_TOKEN` and `SONAR_HOST_URL`. A SonarQube on `localhost` is not reachable from
GitHub runners; use SonarCloud, a public server, or a self-hosted runner.
