# Job Portal — Spring Boot Backend

A production-grade REST API for a full-featured job portal platform. Built with Spring Boot, Spring Security, and MySQL. Supports candidate job search and applications, company job management and hiring pipelines, and admin moderation — all secured with JWT and role-based authorization.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 17, Spring Boot 4.0.1 |
| Security | Spring Security, JWT (JJWT 0.12.5) |
| Persistence | Spring Data JPA, MySQL 8+, Flyway |
| Validation | Jakarta Bean Validation |
| Build | Maven (wrapper included) |
| Testing | JUnit 5, Spring Boot Test, H2 (test scope) |

---

## Key Features

**Candidates (job seekers)**
- Register, log in, and manage a profile
- Browse and search jobs with dynamic filtering
- Save jobs for later
- Apply to jobs and track application status

**Companies (employers)**
- Submit a company registration for admin approval
- Create and manage job listings
- Review applicants, advance or reject candidates through a hiring pipeline
- Degree-field eligibility rules per job listing

**Admins**
- Approve or reject company applications
- Moderate users, companies, and jobs
- Manage degree fields used in eligibility rules

**Cross-cutting**
- JWT authentication with stateless sessions
- Role-based authorization enforced at the service and guard layers
- Disabled or locked accounts are rejected on every authenticated request
- Password reset via email token
- Full request DTO validation with structured error responses

---

## Architecture Highlights

```
controllers/       HTTP layer — request/response only
├── admin/
├── company/
├── user/
└── open/          Public endpoints (no auth required)
services/          Business logic
guards/            Ownership and authorization checks
repositories/      Spring Data JPA
specifications/    JPA Criteria API for dynamic job filtering
mappers/           Entity → DTO conversion
dto/               API input/output types (never raw entities)
exceptions/        Global error handling via @ControllerAdvice
```

- Controllers do not contain business logic.
- Authorization ownership checks are centralized in guards, not scattered in controllers.
- JPA entities are never exposed in API responses — all output goes through DTOs.
- Dynamic job search is implemented with JPA Specifications, keeping queries composable and type-safe.

---

## Database and Migrations

The schema is entirely owned by Flyway. Hibernate runs in `validate` mode — it checks entity mappings against the live schema but emits no DDL.

```
src/main/resources/db/migration/
└── V1__init_schema.sql    # Full schema bootstrap
```

**Starting with a fresh database:**
1. Create an empty MySQL database (`jobsPortal` by default).
2. Start the application — Flyway runs `V1__init_schema.sql` automatically.

Never rely on `ddl-auto=create` or `ddl-auto=update` outside of isolated tests.

---

## Security and Auth

- **JWT** — issued at login, validated on every request via a custom filter.
- **Disabled / locked accounts** — the JWT filter checks the live user state from the database on every request; a valid token is not enough if the account is inactive.
- **Role-based access** — `ROLE_USER`, `ROLE_COMPANY`, `ROLE_SUPER_ADMIN`. Endpoints are protected by role; resource ownership is enforced by guards.
- **Password reset** — time-limited tokens delivered by email; token state is validated server-side.
- **Bootstrap admin** — a single super-admin can be seeded on first startup via configuration (disabled by default; never commit credentials).

---

## Testing

Integration tests run against an H2 in-memory database with Flyway migrations applied, covering:

| Test class | Coverage area |
|---|---|
| `AuthValidationIT` | Registration and login validation |
| `CompanyIsolationIT` | Cross-company data isolation |
| `DisabledUserJwtIT` | JWT enforcement for disabled accounts |
| `PasswordResetIT` | Full password reset flow |
| `ProfileAndCompanyAppValidationIT` | DTO validation on profile and company endpoints |
| `SavedJobMapperTest` | Saved-job DTO ID correctness |
| `JobsApplicationTests` | Spring context smoke test |

Run the full suite:

```
./mvnw.cmd clean verify          # Windows
./mvnw clean verify              # macOS / Linux
```

---

## Local Development Setup

### Prerequisites

- Java 17+
- MySQL 8+ (running locally on port 3306 or 3307)
- No Docker required — the application runs directly from the JVM

### Steps

**1. Clone the repository**
```bash
git clone <repo-url>
cd jobs
```

**2. Create the database**
```sql
CREATE DATABASE jobsPortal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**3. Configure the application**

Windows (PowerShell or CMD):
```
copy src\main\resources\application-example.properties src\main\resources\application.properties
```

macOS / Linux / Git Bash:
```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

Edit `application.properties` and fill in:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jobsPortal
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

app.jwt.secret=YOUR_JWT_SECRET_AT_LEAST_32_CHARS

# Seed the first admin user (set to false after first run)
app.bootstrap.admin.enabled=true
app.bootstrap.admin.username=admin
app.bootstrap.admin.email=admin@example.com
app.bootstrap.admin.password=STRONG_PASSWORD

# Email for password reset (MailHog or real SMTP)
spring.mail.host=localhost
spring.mail.port=1025
```

**4. Run the application**

From IntelliJ: open the project and click the green Run button on `JobsApplication`.

From the terminal:
```bash
./mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # macOS / Linux
```

The API starts on `http://localhost:8080`.

> **Note:** Docker is not part of this project's setup. Run MySQL locally or via any existing local container exposed on the expected port.

> **Frontend:** This repository contains the backend API only. A separate Next.js frontend connects on `http://localhost:3000`.

---

## Example Configuration Reference

See [`src/main/resources/application-example.properties`](src/main/resources/application-example.properties) for all supported configuration keys with inline documentation. Your local `application.properties` is git-ignored.

---

## API Overview

All endpoints are prefixed with `/api`.

| Group | Base path | Access |
|---|---|---|
| Auth | `/api/auth/**` | Public |
| Public jobs & degrees | `/api/open/**` | Public |
| Candidate profile & applications | `/api/user/**` | `ROLE_USER` |
| Company management | `/api/company/**` | `ROLE_COMPANY` |
| Admin moderation | `/api/admin/**` | `ROLE_SUPER_ADMIN` |

**Auth endpoints** — register, login, password reset request and confirm.

**Open endpoints** — browse and filter jobs (title, location, type, degree field), list degree fields.

**User endpoints** — manage profile, save and unsave jobs, apply to jobs, track application status.

**Company endpoints** — manage company profile, create and manage job listings, review applicants, advance or reject candidates in the hiring pipeline.

**Admin endpoints** — review and approve company applications, moderate users and companies, manage degree fields.

Error responses include an HTTP status, a machine-readable `code` field, and a human-readable `message`. Validation errors include per-field detail.

---

## Project Status

Core backend features are complete and covered by integration tests:

- [x] JWT auth with disabled-account enforcement
- [x] Role-based authorization with ownership guards
- [x] Company application and approval workflow
- [x] Job lifecycle management with dynamic search
- [x] Candidate profiles, saved jobs, and applications
- [x] Hiring pipeline with status transitions
- [x] Password reset flow
- [x] Flyway-managed schema with Hibernate validation
- [x] Request DTO validation

Ongoing work:
- Stable machine-readable error codes across all endpoints
- OpenAPI / Swagger documentation
- Expanded integration test coverage
- Audit log for application status changes

This project is under active development. It is not deployed to production.
