# Skill-Based Job Search Portal

A full-stack job search platform that connects job seekers with recruiters through skill-based job matching. Candidates can discover relevant opportunities, apply for jobs and track applications, while recruiters can publish job openings and manage applicants.

<p align="center">
  <a href="https://skill-based-job-search-portal.onrender.com">
    <strong>🌐 Visit Live Application</strong>
  </a>
</p>

**Live Website:** https://skill-based-job-search-portal.onrender.com

---

## Table of Contents

1. [About the Project](#1-about-the-project)
2. [Live Application – How to Use](#2-live-application--how-to-use)
3. [Features](#3-features)
4. [Screenshots](#4-screenshots)
5. [Technology Stack](#5-technology-stack)
6. [Application Architecture](#6-application-architecture)
7. [Prerequisites](#7-prerequisites)
8. [Local Installation and Setup](#8-local-installation-and-setup)
9. [Database Configuration](#9-database-configuration)
10. [Environment Variables](#10-environment-variables)
11. [API Documentation](#11-api-documentation)
12. [Project Structure](#12-project-structure)
13. [Deployment Architecture](#13-deployment-architecture)
14. [Security and Privacy](#14-security-and-privacy)
15. [Testing](#15-testing)
16. [Future Improvements](#16-future-improvements)

---

## 1. About the Project

The Skill-Based Job Search Portal is designed to improve the connection between job seekers and recruiters through skill-based job searching.

Instead of relying only on traditional job titles, the application helps candidates discover opportunities based on their technical skills and professional profiles.

The platform provides three separate workspaces:

- **Candidate:** Search jobs, manage skills, upload resumes, apply for jobs and track applications.
- **Recruiter:** Manage companies, publish job opportunities, review applicants and manage recruitment activities.
- **Admin:** Manage users, companies, job postings, applications and the platform's skill catalogue.

The application uses a Java Spring Boot backend with a responsive frontend built using HTML, CSS and vanilla JavaScript.

---

## 2. Live Application – How to Use

The application is publicly accessible through the following URL:

**https://skill-based-job-search-portal.onrender.com**

You can access it from a laptop, desktop, tablet or mobile device.

### For Candidates

1. Open the live application.
2. Select Register and create an account.
3. Choose the Candidate role.
4. Complete your profile with your personal and professional information.
5. Add your technical skills.
6. Upload your resume (PDF, DOC or DOCX, maximum 5 MB).
7. Explore available job opportunities.
8. Apply for jobs with an optional cover letter.
9. Track your application status.
10. Save interesting jobs for later.

### For Recruiters

1. Open the live application.
2. Register using your email address.
3. Select the Recruiter role.
4. Complete your profile.
5. Create your company profile.
6. Publish job opportunities with required skills and job details.
7. Review applications received for your job postings.
8. View candidate resumes when authorized.
9. Update application statuses, including Under Review, Shortlisted, Rejected and Hired.

### For Administrators

Administrator access is restricted to authorized accounts.

The Admin workspace provides functionality to:

- View platform statistics.
- Manage registered users.
- Activate or deactivate accounts.
- Manage companies and job postings.
- Review applications.
- Manage the shared skills catalogue.

Public registration cannot create administrator accounts.

**Hosting note:** The live application currently uses Render's free hosting tier. The service may become inactive after a period without traffic, so the first request after inactivity may take longer to respond.

---

## 3. Features

### Candidate Dashboard

- Candidate registration and secure login.
- JWT-based authentication.
- Profile creation and management.
- Add and remove technical skills.
- Resume upload and protected resume preview.
- Search jobs by title, company and skills.
- Filter by location, job type and experience.
- View required, matched and missing skills.
- Skill-match percentage.
- Recommended jobs based on candidate skills.
- Apply for jobs with optional cover letters.
- Track submitted applications.
- Withdraw eligible applications.
- Save jobs and remove saved jobs.

### Recruiter Dashboard

- Recruiter registration and authentication.
- Company profile creation and management.
- Publish job opportunities.
- Edit and manage owned job postings.
- Configure job descriptions and requirements.
- Set salary ranges and experience requirements.
- Specify job type and work mode.
- Set application deadlines.
- View applicants for owned job postings.
- Access candidate resumes when authorized.
- Update application statuses.
- View recruitment metrics.

### Admin Dashboard

- Admin-only dashboard.
- View platform statistics.
- Manage candidate and recruiter accounts.
- Activate and deactivate users.
- View registered companies.
- Manage job postings.
- Moderate job statuses.
- View applications.
- Add and remove skills from the shared catalogue.

### Additional Platform Features

- Stateless JWT authentication.
- BCrypt password hashing.
- Role-based authorization.
- DTO-based request and response handling.
- Bean Validation.
- Global exception handling.
- Database transactions.
- Pagination and sorting.
- Job search using Spring Data JPA Specifications.
- Optimistic locking for job updates.
- Auditing timestamps.
- Caffeine caching.
- Scheduled job-expiry processing.
- Optional SMTP email notifications.
- Swagger/OpenAPI documentation.
- Spring Boot Actuator health endpoint.

---

## 4. Screenshots

Screenshots of the application can be added here to help visitors understand the interface.

Recommended screenshots:

| Screenshot | Description |
|---|---|
| Home Page | Landing page and job search |
| Candidate Dashboard | Candidate workspace |
| Job Listings | Available job opportunities |
| Candidate Profile | Skills and resume management |
| Recruiter Dashboard | Recruiter workspace |
| Job Management | Create and manage job postings |
| Applications | Review candidate applications |
| Admin Dashboard | Platform administration |

To add screenshots:

1. Create a folder named `docs/screenshots` in your project.
2. Capture screenshots of the actual running application.
3. Save them in that folder.
4. Reference them in this README using Markdown image syntax.

Example:

```markdown
![Candidate Dashboard](docs/screenshots/candidate-dashboard.png)
```

Only add screenshots that represent the actual deployed application. Do not include candidate personal information, email addresses or private resumes.

---

## 5. Technology Stack

### Backend

- Java 21
- Spring Boot 3.5.7
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate ORM
- JWT Authentication
- BCrypt Password Encoder
- Bean Validation
- Maven

### Frontend

- HTML5
- CSS3
- JavaScript ES6
- Fetch API
- Responsive UI

The frontend uses vanilla JavaScript. React or other frontend frameworks are not required.

### Database and Storage

- MySQL 8 for local development.
- TiDB Cloud for the deployed application.
- Cloudinary for external resume storage.

### Tools and Deployment

- Docker
- Docker Compose
- Git
- GitHub
- GitHub Actions
- Render
- Springdoc OpenAPI
- Postman

---

## 6. Application Architecture

The application follows a layered Spring Boot architecture.

```text
                  CLIENT
                    |
                    |
             HTML / CSS / JS
                    |
                    |
               REST APIs
                    |
                    |
             Spring Security
                JWT Filter
                    |
                    |
               Controllers
                    |
                    |
                Services
                    |
                    |
              Repositories
                    |
                    |
               Hibernate
                    |
             +------+------+
             |             |
             |             |
          TiDB Cloud    Cloudinary
          Database      Resume Storage
```

### Backend Layers

| Layer | Responsibility |
|---|---|
| Controller | Handles HTTP requests and responses |
| Service | Contains business logic |
| Repository | Database access using Spring Data JPA |
| Entity | Represents database tables |
| DTO | Transfers request and response data |
| Security | JWT authentication and authorization |
| Exception | Centralized API error handling |
| Config | Application and security configuration |

---

## 7. Prerequisites

To run the project locally, install:

1. JDK 21.
2. Maven 3.9 or later.
3. MySQL Server 8.
4. Git.
5. Docker Desktop (optional, for Docker-based execution).
6. Spring Tool Suite or IntelliJ IDEA or VS Code.

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

## 8. Local Installation and Setup

### Step 1: Clone the repository

```bash
git clone https://github.com/subhakantapatra04/skill-based-job-search-portal.git
```

### Step 2: Open the project

```bash
cd skill-based-job-search-portal
```

Import the project into your IDE as an existing Maven project using the root `pom.xml`.

### Step 3: Create the local database

Open MySQL Workbench or MySQL command line and execute:

```sql
CREATE DATABASE skill_job_search_app
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### Step 4: Configure database credentials

Open:

```text
src/main/resources/application.properties
```

The local database configuration uses:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/skill_job_search_app?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}

spring.datasource.username=${DB_USERNAME:root}

spring.datasource.password=${DB_PASSWORD:}
```

Configure your local MySQL username and password through environment variables or your IDE Run Configuration.

Do not commit database passwords to GitHub.

### Step 5: Run the application

Using Maven:

```bash
mvn clean spring-boot:run
```

Alternatively:

1. Open `SkillJobSearchApplication.java`.
2. Right-click the class.
3. Select Run As → Spring Boot App.

### Step 6: Open the application

```text
http://localhost:8080
```

Useful local URLs:

| Page | URL |
|---|---|
| Home | http://localhost:8080/ |
| Login | http://localhost:8080/login.html |
| Register | http://localhost:8080/register.html |
| Dashboard | http://localhost:8080/dashboard.html |

### Local seed data

For local development, the application can initialize sample skills, a recruiter, a company, sample jobs, a candidate and an administrator.

Seed-data behavior is controlled through configuration.

Local-only demo credentials are defined in `DataInitializer.java`. Do not use default demo credentials in a public deployment.

---

## 9. Database Configuration

The application uses MySQL-compatible database connectivity.

### Local Development

Local development uses MySQL 8.

```properties
spring.jpa.hibernate.ddl-auto=update
```

Hibernate creates or updates the database schema during local development.

### Production

The deployed application uses TiDB Cloud, which provides MySQL-compatible database connectivity.

The production JDBC connection uses TLS certificate verification.

Example JDBC URL format:

```text
jdbc:mysql://YOUR_TIDB_HOST:4000/skill_job_search_app?sslMode=VERIFY_IDENTITY&serverTimezone=UTC
```

Production database credentials are supplied through environment variables.

For production schema changes, versioned Flyway or Liquibase migrations are recommended instead of automatic schema updates.

---

## 10. Environment Variables

The application supports environment-based configuration.

Never publish actual passwords, API secrets, JWT secrets or other sensitive values in this repository.

### Database

| Variable | Description |
|---|---|
| `DB_URL` | Database JDBC URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |

### Security

| Variable | Description |
|---|---|
| `JWT_SECRET` | JWT signing secret |
| `JWT_EXPIRATION_MS` | JWT expiration time |
| `SEED_DATA` | Controls demo data initialization |
| `ADMIN_EMAIL` | Administrator email for provisioning |
| `ADMIN_PASSWORD` | Administrator password for provisioning |
| `ADMIN_PROVISIONING_ENABLED` | Controls initial administrator provisioning |

Administrator provisioning should be disabled after successful initial account creation.

### Resume Storage

| Variable | Description |
|---|---|
| `RESUME_STORAGE` | Resume storage provider |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret |

### Email

| Variable | Description |
|---|---|
| `MAIL_ENABLED` | Enables or disables email |
| `MAIL_HOST` | SMTP server |
| `MAIL_PORT` | SMTP port |
| `MAIL_USERNAME` | SMTP username |
| `MAIL_PASSWORD` | SMTP password |
| `MAIL_FROM` | Sender email address |

### Other Configuration

| Variable | Description |
|---|---|
| `PORT` | Server port supplied by hosting platform |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate schema management mode |

An `.env.example` file can be used as a reference for local configuration. Spring Boot does not automatically load `.env` files.

---

## 11. API Documentation

The application exposes REST APIs under `/api`.

Protected API requests require JWT authentication.

```http
Authorization: Bearer <JWT_TOKEN>
```

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register Candidate or Recruiter |
| POST | `/api/auth/login` | Authenticate user and receive JWT |

### Jobs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/jobs` | Search and filter jobs |
| GET | `/api/jobs/{id}` | View job details |
| GET | `/api/jobs/recommended` | Get recommended jobs |
| POST | `/api/jobs` | Create job |
| PUT | `/api/jobs/{id}` | Update owned job |
| DELETE | `/api/jobs/{id}` | Close or delete job, as implemented |

### Candidate Profile

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/profile` | View profile |
| PUT | `/api/users/profile` | Update profile |
| POST | `/api/users/skills/{skillId}` | Add skill |
| DELETE | `/api/users/skills/{skillId}` | Remove skill |
| POST | `/api/users/resume` | Upload resume |
| GET | `/api/users/resume/{candidateId}` | Access authorized resume |

Resume upload uses multipart form data with the field name `file`.

### Skills

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/skills` | List available skills |
| POST | `/api/skills` | Admin creates skill |

### Companies

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/companies` | List companies |
| POST | `/api/companies` | Create company |
| GET | `/api/companies/mine` | View owned company |
| PUT | `/api/companies/{id}` | Update company |

### Applications

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/applications/{jobId}` | Apply for a job |
| GET | `/api/applications/my` | View own applications |
| PATCH | `/api/applications/{id}/withdraw` | Withdraw eligible application |
| PATCH | `/api/applications/{id}/status` | Update application status |

### Saved Jobs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/saved-jobs/{jobId}` | Save job |
| DELETE | `/api/saved-jobs/{jobId}` | Remove saved job |
| GET | `/api/saved-jobs` | List saved jobs |

### Recruiter

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/recruiter/dashboard` | Recruiter dashboard statistics |
| GET | `/api/recruiter/jobs` | View owned jobs |
| GET | `/api/recruiter/applications` | View applications for owned jobs |

### Admin

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/dashboard` | Admin dashboard statistics |
| GET | `/api/admin/users` | View users |
| GET | `/api/admin/companies` | View companies |
| GET | `/api/admin/jobs` | View jobs |
| GET | `/api/admin/applications` | View applications |
| GET | `/api/admin/skills` | View skills |
| PATCH | `/api/admin/users/{id}/active` | Activate or deactivate user |
| PATCH | `/api/admin/jobs/{id}/status` | Change job status |
| POST | `/api/admin/skills` | Add skill |
| DELETE | `/api/admin/skills/{id}` | Remove skill, if supported by the configured route |

### API Documentation URLs

```text
/swagger-ui.html
/v3/api-docs
/actuator/health
```

---

## 12. Project Structure

```text
skill-based-job-search-portal/
│
├── src/
│   ├── main/
│   │   ├── java/com/jobsearch/skilljob/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── enums/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── css/
│   │           ├── js/
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── register.html
│   │           └── dashboard.html
│   │
│   └── test/
│
├── docs/
├── database/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 13. Deployment Architecture

The application is deployed using Docker and Render.

### Production Components

| Component | Technology | Responsibility |
|---|---|---|
| Hosting | Render | Hosts Spring Boot application |
| Backend | Spring Boot | Business logic and REST APIs |
| Database | TiDB Cloud | Persistent application data |
| Resume Storage | Cloudinary | External resume file storage |
| Source Control | GitHub | Source code management |
| Build | Docker | Containerized deployment |

### Production Flow

```text
              PUBLIC USERS
                   |
                   |
                HTTPS
                   |
                   |
           RENDER WEB SERVICE
             Spring Boot
                   |
             +-----+-----+
             |           |
             |           |
          TiDB Cloud   Cloudinary
          Database     File Storage
```

### Live Deployment

**Website:**

https://skill-based-job-search-portal.onrender.com

The live application uses external database and resume storage services. Your local computer does not need to remain switched on for the deployed application to work.

The current hosting configuration uses a free Render service, which may spin down after inactivity.

---

## 14. Security and Privacy

The application implements several security mechanisms.

- JWT-based stateless authentication.
- BCrypt password hashing.
- Role-based authorization for Candidate, Recruiter and Admin.
- Restricted administrator registration.
- Protected candidate resume retrieval.
- Recruiter resume access restricted by application ownership and authorization rules.
- Cloudinary credentials managed through environment variables.
- Database access restricted using TiDB authorized-network rules.
- HTTPS for the deployed application.
- Database passwords and API keys excluded from Git.

### Privacy Considerations

Resumes contain personal information. They are stored externally and are retrieved through the application's protected backend endpoint.

Only authenticated users who satisfy the application's authorization rules can access candidate resumes.

Before using the application for real recruitment at scale, additional privacy policies, account deletion procedures, data retention rules, monitoring and security reviews should be maintained.

---

## 15. Testing

Run the automated tests using:

```bash
mvn test
```

Run a complete Maven verification:

```bash
mvn clean verify
```

Build the Docker image:

```bash
docker compose build
```

Start the local application:

```bash
docker compose up -d
```

Check running containers:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f app
```

Stop containers:

```bash
docker compose down
```

Do not use `docker compose down -v` unless you intentionally want to remove persistent volumes and understand the data-loss consequences.

---

## 16. Future Improvements

Potential improvements for future versions include:

- Advanced skill-matching algorithms.
- AI-based job recommendations.
- Resume parsing and skill extraction.
- Automated job discovery.
- Advanced recruiter analytics.
- Email and notification improvements.
- Interview scheduling.
- Integration with external job platforms.
- Automated integration and security testing.
- Performance optimization and load testing.
- Production monitoring and database backup automation.

---

## Project Information

**Project:** Skill-Based Job Search Portal

**Backend:** Java 21, Spring Boot 3.5.7

**Frontend:** HTML, CSS and Vanilla JavaScript

**Database:** MySQL 8 / TiDB Cloud

**File Storage:** Cloudinary

**Hosting:** Render

**Repository:** https://github.com/subhakantapatra04/skill-based-job-search-portal

**Live Application:** https://skill-based-job-search-portal.onrender.com

---

Developed as a full-stack Java project focused on connecting job seekers and recruiters through skill-based job matching.