# Architecture overview

```mermaid
flowchart TD
  Browser[HTML / CSS / Vanilla JavaScript] -->|Fetch + Bearer JWT| API[Spring MVC REST Controllers]
  API --> Security[Spring Security + JWT Filter]
  Security --> Services[Service / Business Logic]
  Services --> Repositories[Spring Data JPA Repositories]
  Repositories --> Hibernate[Hibernate ORM]
  Hibernate --> DB[(MySQL 8)]
  Services --> Mail[Optional SMTP Notifications]
  Scheduler[Daily Job Expiry Scheduler] --> Repositories
  API --> Docs[OpenAPI / Swagger]
```

## Main entity relationships

```mermaid
erDiagram
  USERS ||--o{ COMPANIES : owns
  USERS ||--o{ JOB_APPLICATIONS : submits
  USERS ||--o{ SAVED_JOBS : bookmarks
  USERS }o--o{ SKILLS : has
  COMPANIES ||--o{ JOBS : publishes
  JOBS }o--o{ SKILLS : requires
  JOBS ||--o{ JOB_APPLICATIONS : receives
  JOBS ||--o{ SAVED_JOBS : bookmarked_as
```

## Role model

- `CANDIDATE`: manage own profile, skills, resume, applications and saved jobs.
- `RECRUITER`: manage owned companies and jobs; review applicants for owned jobs.
- `ADMIN`: manage platform accounts, skill catalogue and job moderation; inspect companies and applications.

## Security notes

Passwords are BCrypt encoded. JWTs are stateless bearer tokens. Admin registration is disabled. Candidate resume retrieval is authorized for the candidate, an admin, or a recruiter who has received an application from that candidate. CORS is configured for local development; replace the localhost patterns before deployment.
