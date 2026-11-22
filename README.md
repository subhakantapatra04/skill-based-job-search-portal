# Skill-Based Job Search Portal

A full-stack job portal built with Java 21, Spring Boot 3, Spring Security/JWT, Spring Data JPA/Hibernate, MySQL 8, and HTML/CSS/vanilla JavaScript. The application has separate Candidate, Recruiter, and Admin workspaces.

> \\\*\\\*Local development starter credentials are seeded automatically. Change them before deploying anywhere public.\\\*\\\* This repository is a development-ready project. Before a public production launch, configure HTTPS, a strong external JWT secret, a private resume storage service, production email, backups, and deployment secrets.

## 1\. Technology

* Java 21, Maven, Spring Boot 3.5.7
* Spring MVC, Spring Data JPA, Hibernate, MySQL 8
* Spring Security, stateless JWT, BCrypt password hashing, role-based authorization
* Bean Validation, DTOs, global exception handling
* JPA Specifications, pagination, sorting, transactions, optimistic locking, auditing
* Caffeine caching, scheduled job-expiry task, optional SMTP email notifications
* Springdoc OpenAPI / Swagger UI, Spring Boot Actuator
* HTML5, CSS3, JavaScript ES6 and Fetch API (no React)

## 2\. Prerequisites

1. JDK 21 (set `JAVA\\\_HOME`).
2. MySQL Server 8 running locally.
3. Maven 3.9+ (STS includes Maven integration; command-line Maven is optional if you run from STS).

## 3\. Database setup

Open MySQL Workbench or the MySQL command line and run:

```sql
CREATE DATABASE skill\\\_job\\\_search\\\_app CHARACTER SET utf8mb4 COLLATE utf8mb4\\\_unicode\\\_ci;
```

The application uses Hibernate `ddl-auto=update` for local development and creates/updates the tables at startup. Do not use `ddl-auto=update` as a production migration strategy; use versioned Flyway/Liquibase migrations before production deployment.

## 4\. Configure database credentials

Open `src/main/resources/application.properties`. The defaults are:

```properties
spring.datasource.url=${DB\\\_URL:jdbc:mysql://localhost:3306/skill\\\_job\\\_search\\\_app?createDatabaseIfNotExist=true\\\&useSSL=false\\\&serverTimezone=UTC}
spring.datasource.username=${DB\\\_USERNAME:root}
spring.datasource.password=${DB\\\_PASSWORD:}
```

Set your local MySQL password in `DB\\\_PASSWORD` (recommended) or replace the default empty value in `application.properties` for local use. Do not commit real passwords to GitHub.

Optional environment variables are listed in `.env.example`. Spring Boot does not automatically load `.env`; configure variables in your operating system, IDE Run Configuration, or a local environment manager.

## 5\. Import into Spring Tool Suite (STS)

1. Extract `SkillBasedJobSearchPortal\\\_Full.zip` to a normal workspace folder.
2. Open STS.
3. Select **File → Import → Maven → Existing Maven Projects**.
4. Browse to the extracted `SkillBasedJobSearchPortal\\\_Full` folder.
5. Select the `pom.xml` project and click **Finish**.
6. Wait for Maven dependencies to download.
7. Open `SkillJobSearchApplication.java`.
8. Right-click → **Run As → Spring Boot App**.
9. Open `http://localhost:8080`.

Run the automated context/seed-data checks with `mvn test` (the tests use an in-memory H2 database and do not require your MySQL instance).

You can also run from a terminal in the project folder:

```bash
mvn clean spring-boot:run
```

If Maven reports a Java version mismatch, configure STS to use JDK 21: **Window → Preferences → Java → Installed JREs** and **Project → Properties → Java Compiler**.

## 6\. Seeded local accounts

The first startup seeds sample skills, a demo recruiter, a demo company, three sample jobs, a demo candidate and an administrator.

|Role|Email|Password|
|-|-|-|
|Admin|`admin@skilljobs.local`|`Admin@12345`|
|Recruiter|`recruiter@skilljobs.local`|`Recruiter@123`|
|Candidate|`candidate@skilljobs.local`|`Candidate@123`|

Admin credentials can be changed with `ADMIN\\\_EMAIL` and `ADMIN\\\_PASSWORD` environment variables **before the first seed run**. Existing users are not overwritten. To reset a local demo database, back up anything important, drop and recreate the database, then restart. Never use these demo credentials in a public deployment.

Registration is open for Candidate and Recruiter accounts. Public registration cannot create an Admin. Admin is provisioned by the startup seed or directly by a trusted database administrator.

## 7\. Features

### Candidate

* Register and sign in using JWT authentication.
* View candidate dashboard and profile.
* Update name, phone, location and professional headline.
* Add/remove skills from the shared skill catalogue.
* Upload PDF/DOC/DOCX resume (5 MB maximum); resume download is access-checked.
* Search jobs by title/company/skill, location, job type and experience.
* View required, matched and missing skills and skill-match percentage.
* View recommended jobs based on profile skills.
* Apply once per job with an optional cover letter.
* Track application status and withdraw eligible applications.
* Save jobs and remove saved jobs.

### Recruiter

* Register and sign in.
* Create and manage company profiles.
* Post jobs with description, location, salary range, experience, job type, work mode, deadline and required skills.
* View, edit and close owned job postings.
* View applicants for owned postings, inspect resumes when authorized, and update application status (Under Review, Shortlisted, Rejected, Hired).
* View hiring metrics on the recruiter dashboard.

### Admin

* Admin-only dashboard and platform metrics.
* View candidate and recruiter accounts; activate/deactivate accounts.
* View registered companies and all applications.
* Moderate job status (open, closed, expired, draft).
* Add and remove skills in the shared catalogue.

### Platform capabilities

* JWT bearer-token authentication, BCrypt password storage, stateless sessions and role-based authorization.
* DTO-based API boundaries, request validation and consistent API error responses.
* JPA relationships, transactions, auditing timestamps and optimistic locking on jobs.
* Job search using Spring Data JPA Specifications, pagination and sorting.
* Scheduled daily expiry for open jobs whose deadline has passed.
* Caffeine cache for the public skills catalogue.
* Optional email notifications for application submission and status updates.
* Swagger UI and Actuator health endpoint.

## 8\. Useful URLs

* Website: `http://localhost:8080/`
* Login: `http://localhost:8080/login.html`
* Register: `http://localhost:8080/register.html`
* Dashboard: `http://localhost:8080/dashboard.html`
* Swagger UI: `http://localhost:8080/swagger-ui.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`
* Health: `http://localhost:8080/actuator/health`

## 9\. Main API routes

All protected API calls use `Authorization: Bearer <JWT>`.

|Area|Method and route|Purpose|
|-|-|-|
|Auth|`POST /api/auth/register`|Candidate/recruiter registration|
|Auth|`POST /api/auth/login`|Login and receive JWT|
|Jobs|`GET /api/jobs?q=java\\\&location=Bangalore\\\&page=0\\\&size=10`|Search/filter jobs|
|Jobs|`GET /api/jobs/{id}`|Job details|
|Jobs|`GET /api/jobs/recommended`|Candidate recommendations|
|Jobs|`POST /api/jobs`, `PUT /api/jobs/{id}`, `DELETE /api/jobs/{id}`|Recruiter create/update/close|
|Profile|`GET/PUT /api/users/profile`|Candidate profile|
|Profile|`POST/DELETE /api/users/skills/{skillId}`|Candidate skills|
|Resume|`POST /api/users/resume` (multipart field `file`)|Upload resume|
|Resume|`GET /api/users/resume/{candidateId}`|Authorized resume access|
|Skills|`GET /api/skills`|Public skill catalogue|
|Skills|`POST /api/skills`|Admin-only create skill|
|Companies|`GET /api/companies`, `POST /api/companies`, `GET /api/companies/mine`, `PUT /api/companies/{id}`|Company management|
|Applications|`POST /api/applications/{jobId}`|Candidate applies|
|Applications|`GET /api/applications/my`|Candidate application history|
|Applications|`PATCH /api/applications/{id}/withdraw`|Candidate withdraws|
|Applications|`PATCH /api/applications/{id}/status`|Recruiter updates status|
|Saved jobs|`POST/DELETE /api/saved-jobs/{jobId}`, `GET /api/saved-jobs`|Candidate bookmarks|
|Recruiter|`GET /api/recruiter/dashboard`, `/jobs`, `/applications`|Recruiter workspace data|
|Admin|`GET /api/admin/dashboard`, `/users`, `/companies`, `/jobs`, `/applications`, `/skills`|Admin views|
|Admin|`PATCH /api/admin/users/{id}/active`|Activate/deactivate account|
|Admin|`PATCH /api/admin/jobs/{id}/status`|Moderate job|
|Admin|`POST/DELETE /api/admin/skills`|Manage skills|

## 10\. Optional email notifications

Email is disabled by default, so the application runs without an SMTP account. To enable it, set `MAIL\\\_ENABLED=true` and configure `MAIL\\\_HOST`, `MAIL\\\_PORT`, `MAIL\\\_USERNAME`, `MAIL\\\_PASSWORD`, `MAIL\\\_SMTP\\\_AUTH`, `MAIL\\\_SMTP\\\_STARTTLS`, and `MAIL\\\_FROM`. For local testing, MailHog or another local SMTP catcher can be used. Do not commit SMTP credentials.

## 11\. Project structure

```text
src/main/java/com/jobsearch/skilljob
  config/       Security, seed data
  controller/   REST endpoints
  dto/          Request and response records
  entity/       JPA entities
  enums/        Roles, job and application states
  exception/    API exceptions and global handler
  repository/   Spring Data repositories
  security/     JWT filter, JWT service, user details
  service/      Business logic, notifications, scheduled expiry
src/main/resources
  application.properties
  static/       HTML, CSS and JavaScript frontend
 database/      Local database setup SQL
 docs/          Architecture and ER diagram
```

## 12\. Before a public GitHub/live deployment

* Change demo credentials and use a long random `JWT\\\_SECRET` (at least 32 bytes).
* Set `SEED\\\_DATA=false` after initial provisioning; remove or rotate demo accounts.
* Use HTTPS and secure CORS to the actual frontend domain.
* Move resumes to private object storage; do not serve candidate documents from a public static folder.
* Configure production email, logging, monitoring, database backups and restore procedures.
* Use Flyway/Liquibase migrations and production database permissions.
* Add deployment-specific integration tests, security review, rate limiting and privacy/retention policies.

The included implementation is intended for local learning, demonstration and portfolio development. Public production deployment requires the hardening steps above; do not publish demo credentials, real resumes, or personal data.

