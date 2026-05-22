# Roadmap to a serious job portal

## North star

A serious job portal is not just "users can apply and companies can post jobs."

It needs five reliable systems:

- **Candidate system**: profile, resume, saved jobs, applications, notifications, status tracking.
- **Employer system**: company profile, job lifecycle, applicant pipeline, hiring notes, team access.
- **Admin system**: moderation, company verification, user/company/job/application management.
- **Trust/security system**: auth, authorization, rate limits, audit logs, file verification, anti-abuse.
- **Operational system**: migrations, Docker, CI/CD, observability, tests, backups, deploy profiles.

Your current code has a usable MVP skeleton, but it is not yet serious. The roadmap below is the order to execute.

---

## Phase 0 — Stop the bleeding

This is the "do not build more features until this is fixed" phase.

### 0.1 Fix the three current correctness bugs

**Fix `JobServiceImpl.updateJob()`**

Current bug: calls `jobGuard.requireOwnedActiveCompanyJob(userId, jobId)` with reversed arguments.

Target:
```java
Job job = jobGuard.requireOwnedActiveCompanyJob(jobId, userId);
```

Add integration tests:
- `companyOwner_canUpdateOwnJob_returns200()`
- `companyOwner_cannotUpdateOtherCompanyJob_returns404()`

**Fix `JobServiceImpl.deleteJob()`**

Same reversed argument bug. Target:
```java
Job job = jobGuard.requireOwnedActiveCompanyJob(jobId, userId);
```

Add integration tests:
- `companyOwner_canDeleteOwnJob_returnsNoContent()`
- `companyOwner_cannotDeleteOtherCompanyJob_returns404()`

Also change `CompanyJobsController.deleteJob()` from `ResponseEntity.ok("Job deleted successfully.")` to a `@ResponseStatus(HttpStatus.NO_CONTENT) void` method. A delete endpoint should not return a random string.

**Fix `SavedJobMapper.toResponse()`**

Current bug: `SavedJobResponse.id` is actually `savedJob.getJob().getId()`.

Replace `SavedJobResponse` with:
```java
public record SavedJobResponse(
        Long savedJobId,
        Long jobId,
        String title,
        String tagline,
        EmploymentType employmentType,
        String level,
        BigDecimal payMin,
        BigDecimal payMax,
        PayPeriod payPeriod,
        PayType payType,
        String location,
        JobStatus status,
        String companyName
) {}
```

Update `SavedJobMapper.toResponse()` to set `savedJob.getId()` as `savedJobId` and `savedJob.getJob().getId()` as `jobId`. This matters for the frontend — a cache will break if `id` sometimes means the saved row and sometimes the job.

### 0.2 Rename dangerous/misleading methods

- Rename typo: `JobGuard.requredCompanyOwnedJob(...)` → `requireCompanyOwnedJob(...)`
- Rename or fix `JobApplicationGuard.requireCompanyOwnedEnabledPendingApplication(...)`: the method does not enforce PENDING, so either rename it to `requireCompanyOwnedEnabledApplication(...)` or create a separate `ApplicationStatusTransitionGuard`. Recommendation: allow company to move `PENDING → INTERVIEW → REJECTED` or `INTERVIEW → HIRED`, so rename the method and add a proper transition guard.

### 0.3 Clean dead/security-theater classes

Delete or implement:
- `SavedJobGuard` — currently empty; implies protection that does not exist.
- `ValidateCompanyOwnership` — duplicates guard behavior.

### 0.4 Rename `services/iml` to `services/impl`

Current misspelled package/class names: `CompanyServiceIml`, `SavedJobServiceIml`, `services.iml`.
Target: `CompanyServiceImpl`, `SavedJobServiceImpl`, `services.impl`.

---

## Phase 1 — Make the database production-real

### 1.1 Create real initial schema migration

Replace the current migration story with a true baseline. Current migrations assume tables already exist; that is not safe for a fresh database.

Migration plan:
- `V1__initial_schema.sql` — creates all tables from scratch
- `V2__seed_roles.sql`
- `V3__create_admin_bootstrap_token_or_admin_seed.sql`
- `V4__indexes_and_constraints.sql`

Tables `V1` must create: `roles`, `users`, `companies`, `company_applications`, `degree_fields`, `candidate_profiles`, `jobs`, `job_degree_fields`, `job_benefits`, `job_minimum_requirements`, `job_applications`, `job_application_status_audits`, `saved_jobs`, `password_reset_tokens`, `candidate_experiences`, `candidate_certificates`.

### 1.2 Set Hibernate to validate, not mutate

Production:
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

Avoid `ddl-auto=update` in production — it is a footgun.

### 1.3 Add missing DB constraints

```sql
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE roles ADD CONSTRAINT uk_roles_role_name UNIQUE (role_name);
```

Application-level uniqueness checks are not enough — two simultaneous requests can race.

### 1.4 Decide the role model

**Simpler MVP path** — keep single-role (`ROLE_USER`, `ROLE_COMPANY`, `ROLE_SUPER_ADMIN`).

**Serious long-term path** — `User → ManyToMany Role` with a `user_roles` join table.

Recommendation: keep single-role for now but design DTOs as if multiple roles are possible:
```json
{ "userId": 1, "username": "ali", "roles": ["ROLE_USER"] }
```

---

## Phase 2 — Harden authentication and security

### 2.1 Remove hardcoded super admin credentials

`DataInitializer.seedData()` currently creates `username: superAdmin / password: superPass`. Replace with env-based bootstrap:

```properties
app.bootstrap.admin.username=${BOOTSTRAP_ADMIN_USERNAME}
app.bootstrap.admin.email=${BOOTSTRAP_ADMIN_EMAIL}
app.bootstrap.admin.password=${BOOTSTRAP_ADMIN_PASSWORD}
```

No fixed password in source code.

### 2.2 Fix JWT disabled-user behavior

`JwtAuthenticationFilter` must check account state before setting authentication:

```java
if (!userDetails.isEnabled()
        || !userDetails.isAccountNonLocked()
        || !userDetails.isAccountNonExpired()
        || !userDetails.isCredentialsNonExpired()) {
    SecurityContextHolder.clearContext();
    authEntryPoint.commence(request, response, new DisabledException("Account disabled or locked"));
    return;
}
```

Otherwise a disabled user's old JWT keeps working until expiry.

### 2.3 Add refresh token strategy

Recommendation — access token in memory + refresh token as httpOnly secure cookie:

- Short-lived access token returned in JSON
- Refresh token as httpOnly cookie
- Endpoints: `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`, `POST /api/auth/logout-all`
- Table: `refresh_tokens(id, user_id, token_hash, expires_at, revoked_at, created_at, user_agent, ip_address)`

### 2.4 Add email verification

`register()` currently creates an enabled user immediately. A serious portal needs:

- `email_verified` / `email_verified_at` fields on `User`
- `POST /api/auth/verify-email/request`
- `POST /api/auth/verify-email/confirm`
- Candidates cannot apply and companies cannot submit applications until email is verified.

### 2.5 Fix password reset errors

`UserServiceImpl.resetPassword()` throws raw `RuntimeException`. Replace with `ApiException`. Avoid email enumeration — `forgot-password` should always return `204`, even if the email is unknown.

### 2.6 Add rate limiting

| Endpoint | Limit |
|---|---|
| `POST /api/auth/login` | 5 attempts / 15 min / username + IP |
| `POST /api/auth/forgot-password` | 3 attempts / hour / email + IP |
| `POST /api/auth/register` | 5 attempts / hour / IP |
| `POST /api/jobs/{jobId}/apply` | reasonable per-user throttle |

Use Bucket4j or a Redis-backed limiter.

### 2.7 Add CORS config for Next.js

```properties
app.cors.allowed-origins=http://localhost:3000,https://yourfrontend.com
```

Add `CorsConfigurationSource` bean. Do not use wildcard CORS with credentials.

### 2.8 Add security audit logs

Table `audit_events(id, actor_user_id, actor_role, action, resource_type, resource_id, ip_address, user_agent, created_at, metadata_json)`.

Log: `LOGIN_SUCCESS`, `LOGIN_FAILURE`, `PASSWORD_RESET_REQUESTED`, `PASSWORD_RESET_COMPLETED`, `COMPANY_APPLICATION_APPROVED`, `COMPANY_APPLICATION_REJECTED`, `USER_DISABLED`, `USER_LOCKED`, `COMPANY_DISABLED`, `JOB_PUBLISHED`, `JOB_CLOSED`, `APPLICATION_STATUS_CHANGED`.

---

## Phase 3 — Standardize API contract before Next.js

### 3.1 Add OpenAPI

Add `springdoc-openapi`. Expose `GET /v3/api-docs` and `GET /swagger-ui.html`. Generate TypeScript types for Next.js — the frontend should not manually retype backend DTOs.

### 3.2 Normalize error responses

Target format:
```json
{
  "status": 400,
  "error": "Bad Request",
  "code": "PROFILE_INCOMPLETE",
  "message": "Please complete your profile before applying.",
  "path": "/api/jobs/12/apply",
  "timestamp": "2026-05-22T10:00:00",
  "requestId": "..."
}
```

Stable error codes: `AUTH_INVALID_CREDENTIALS`, `AUTH_ACCOUNT_DISABLED`, `AUTH_TOKEN_EXPIRED`, `USER_EMAIL_ALREADY_EXISTS`, `USER_USERNAME_ALREADY_EXISTS`, `PROFILE_INCOMPLETE`, `PROFILE_LOCKED_BY_ACTIVE_APPLICATION`, `JOB_NOT_FOUND`, `JOB_NOT_PUBLISHED`, `JOB_ALREADY_APPLIED`, `JOB_ALREADY_SAVED`, `COMPANY_DISABLED`, `COMPANY_APPLICATION_ALREADY_EXISTS`, `APPLICATION_INVALID_STATUS_TRANSITION`.

The frontend must branch on `code`, not English message strings.

### 3.3 Return all validation errors

`GlobalExceptionHandler.handleValidationExceptions()` should return field-level errors:
```json
{
  "code": "VALIDATION_ERROR",
  "fieldErrors": [
    { "field": "title", "message": "Title is required" }
  ]
}
```

### 3.4 Normalize ID names

Fix ambiguous DTOs — use `savedJobId`, `companyApplicationId`, `applicationId`, etc. Never a bare `id` when the response contains more than one domain object.

`WithdrawJobApplicationResponse` should include `applicationId`, `jobId`, `userId`, `status`, `updatedAt`.

### 3.5 Stop leaking entities in API responses

`JobResponse` currently contains `Set<DegreeField>` (a JPA entity). Replace with:
```java
public record JobDegreeFieldResponse(Long degreeFieldId, String name) {}
```

No API response may expose JPA entities directly.

### 3.6 Add enum/options endpoint

```
GET /api/public/options
```

Returns all frontend dropdown values: `employmentTypes`, `jobStatuses`, `jobApplicationStatuses`, `payPeriods`, `payTypes`, `educationLevels`.

### 3.7 Add a session endpoint

```
GET /api/auth/session
```

Returns a single object the Next.js router can use for all auth checks: `authenticated`, `userId`, `username`, `email`, `roles`, `enabled`, `emailVerified`, `companyApplicationStatus`, `companyId`, `profileComplete`.

---

## Phase 4 — Validation hardening

### 4.1 Harden `CreateJobRequest`

Add `@NotBlank`, `@Size`, `@NotNull`, `@PositiveOrZero` annotations. Keep `JobPayValidator`.

### 4.2 Harden `UpdateJobRequest`

Non-null values must be validated at service level: title/description cannot be blank if present, pay cannot be negative, benefits/requirements cannot contain blank strings.

### 4.3 Harden candidate profile updates

`CandidateProfileUpdateRequest` needs `@PositiveOrZero @Digits(integer=2, fraction=1)` on `yearsExperience`, `@Size(max=30)` on `phone`, `@Size(max=120)` on `location`. Reject blank strings after trimming.

### 4.4 Harden upload references

Replace raw `resumeUrl` / `documentUrl` inputs with upload record references.

Add `uploaded_files(id, owner_user_id, public_id, url, purpose, mime_type, size_bytes, verified, created_at)`.

Endpoints: `POST /api/uploads/sign`, `POST /api/uploads/confirm`.

Profile/company-application endpoints should receive `{ "uploadedFileId": 123 }`, not arbitrary client URLs.

---

## Phase 5 — Candidate experience

### 5.1 Candidate profile v2

Add full experience and certificate CRUD:

```
GET/PATCH /api/users/me/profile
PUT /api/users/me/profile/resume
GET/POST/PATCH/DELETE /api/users/me/profile/experiences/{experienceId}
GET/POST/PATCH/DELETE /api/users/me/profile/certificates/{certificateId}
```

Add profile completion score: `{ "complete": true, "completionPercentage": 85, "missingFields": ["resume"] }`.

### 5.2 Application status visibility

`GET /api/job-applications/me` should return `applicationId`, `jobId`, `jobTitle`, `companyId`, `companyName`, `status`, `appliedAt`, `lastUpdatedAt`, `withdrawable`.

Add `GET /api/job-applications/me/ids` for job listing pages (mirrors the existing saved-jobs IDs endpoint).

### 5.3 Saved jobs v2

- Only save published jobs from enabled companies (fix `SavedJobServiceImpl.saveJob()` to use a query that filters by status and company enabled state).
- Add `DELETE /api/me/saved-jobs/by-saved-id/{savedJobId}`.

### 5.4 Job alerts

Table: `candidate_job_alerts(id, user_id, keyword, location, employment_type, min_pay, degree_field_id, frequency, active, created_at)`.

Endpoints: `GET/POST/PATCH/DELETE /api/me/job-alerts/{alertId}`.

---

## Phase 6 — Employer/company experience

### 6.1 Company profile management

Add `PATCH /api/company/profile`, `PUT /api/company/profile/logo`, `GET /api/public/companies/{companyId}`, `GET /api/public/companies/{companyId}/jobs`.

Expand `Company` entity: `description`, `websiteUrl`, `industry`, `sizeRange`, `location`, `foundedYear`, `logoPublicId`, `logoUrl`.

### 6.2 Job lifecycle rules

Valid transitions: `DRAFT → PUBLISHED`, `PUBLISHED → CLOSED`. Create `JobStatusTransitionGuard`. Keep `DELETE` only for drafts with no applications; use `PATCH /status` to close published jobs.

### 6.3 Applicant pipeline

Explicit transition matrix:
- `PENDING → INTERVIEW`, `PENDING → REJECTED` (by company)
- `INTERVIEW → HIRED`, `INTERVIEW → REJECTED` (by company)
- `PENDING → WITHDRAWN`, `INTERVIEW → WITHDRAWN` (by candidate only)
- `HIRED`, `REJECTED`, `WITHDRAWN` — no further transitions

Implement `ApplicationStatusTransitionGuard`.

### 6.4 Employer notes

Table: `application_notes(id, application_id, author_user_id, note, created_at, updated_at)`.

Endpoints: `GET/POST/PATCH/DELETE /api/company/job-applications/{applicationId}/notes/{noteId}`.

### 6.5 Company team members (later)

Table: `company_members(id, company_id, user_id, role, invited_by_user_id, status, created_at)`.

Roles: `OWNER`, `ADMIN`, `RECRUITER`, `VIEWER`.

Design for this now; do not hardcode everything to `Company.owner` forever.

---

## Phase 7 — Admin system

### 7.1 Admin user management

`GET /api/admin/users` with filters: keyword, role, enabled, accountNonLocked, createdFrom/To.
`GET /api/admin/users/{userId}`, `PATCH /api/admin/users/{userId}/moderation`, `GET /api/admin/users/{userId}/audit-events`.

### 7.2 Admin company management

`GET /api/admin/companies` with filters, `PATCH /api/admin/companies/{companyId}/enabled`, `GET /api/admin/companies/{companyId}/jobs`, `GET /api/admin/companies/{companyId}/applications`.

### 7.3 Company application moderation v2

Rejection should require a reason body:
```json
{ "reason": "Document is unreadable." }
```

Store `reviewed_by_user_id`, `reviewed_at`, `rejection_reason`.

### 7.4 Admin job moderation

`GET /api/admin/jobs`, `PATCH /api/admin/jobs/{jobId}/moderation` — set `hidden`, `hidden_reason`, `moderated_by`, `moderated_at`. A serious portal removes scam jobs without deleting them.

---

## Phase 8 — Public job search and SEO

### 8.1 Better public job filters

Add to `GET /api/public/jobs`: `degreeFieldId`, `payPeriod`, `payType`, `remoteOnly`, `level`, `postedWithinDays`, `sort` (newest / pay_high_to_low / relevance).

### 8.2 Public job detail

Return a structured company sub-object:
```json
{ "company": { "companyId": 10, "name": "Acme", "logoUrl": "...", "location": "...", "websiteUrl": "..." } }
```

Create `PublicJobDetailResponse` and `PublicJobCardResponse` — do not reuse one giant `JobResponse` everywhere.

### 8.3 Slugs

Add `slug` to `jobs` and `companies`. Public routes: `GET /api/public/jobs/{jobIdOrSlug}`, `GET /api/public/companies/{companySlug}`.

### 8.4 Search engine (later)

Start with database search. Add Meilisearch/Typesense/OpenSearch when DB search becomes inadequate.

---

## Phase 9 — Notifications and email system

### 9.1 Notification table

Table: `notifications(id, user_id, type, title, body, read_at, created_at, metadata_json)`.

Endpoints: `GET /api/me/notifications`, `PATCH /api/me/notifications/{id}/read`, `PATCH /api/me/notifications/read-all`.

### 9.2 Email events

Send emails for: company application approved/rejected, candidate application received, application moved to interview, candidate rejected/hired, password reset, email verification, job alert matches.

Move toward `ApplicationEventPublisher` → `NotificationService` → `EmailNotificationWorker`.

---

## Phase 10 — File upload and document verification

### 10.1 Add upload signing

`POST /api/uploads/sign` → returns `{ "uploadUrl": "...", "publicId": "...", "expiresAt": "..." }`.
`POST /api/uploads/confirm` → confirms the upload and marks it verified.

### 10.2 Enforce file rules

| Purpose | Allowed types | Max size |
|---|---|---|
| Resume | pdf, doc, docx | 5 MB |
| Company document | pdf, jpg, png | 10 MB |
| Company logo | jpg, png, webp | 2 MB |

### 10.3 Admin document review

Admin company application view should show: document URL, type, upload date, review status, reviewed by/at, rejection reason.

---

## Phase 11 — Docker, environments, and deployment

### 11.1 Align Java versions

`pom.xml` says Java 17; current Dockerfile uses Java 24. Pick one. Recommendation: Java 21 LTS.

### 11.2 Production Dockerfile

Multi-stage build: Maven build stage → JRE runtime stage. Add a non-root `appuser`. Run tests in CI, not in Docker.

### 11.3 Add `docker-compose.yml`

Local stack: `backend`, `mysql`, `mailhog`, `redis`. Later: `minio`, search, `prometheus`, `grafana`.

### 11.4 Split configs by profile

Files: `application.yml`, `application-dev.yml`, `application-test.yml`, `application-prod.yml`.

Production must require env vars for all secrets: `SPRING_DATASOURCE_URL`, `APP_JWT_SECRET`, `APP_FRONTEND_URL`, `MAIL_HOST`, etc.

### 11.5 Add Actuator

Expose `GET /actuator/health`, `/actuator/info`, `/actuator/metrics`. Lock down everything except health.

---

## Phase 12 — Test strategy

### 12.1 Critical integration tests

**Jobs**: `company_canCreateJob`, `company_canUpdateOwnJob`, `company_cannotUpdateOtherCompanyJob`, `company_canCloseOwnJob`, `disabledCompany_cannotCreateJob`.

**Applications**: `user_canApplyToPublishedEnabledJob`, `user_cannotApplyWithIncompleteProfile`, `user_cannotApplyTwice`, `user_cannotApplyToDraftJob`, `user_cannotApplyToDisabledCompanyJob`, `company_canMoveApplicationToInterview`, `company_cannotMutateOtherCompanyApplication`, `user_canWithdrawOwnPendingApplication`.

**Saved jobs**: `user_canSavePublishedJob`, `user_cannotSaveDraftJob`, `user_cannotSaveDisabledCompanyJob`, `user_cannotSaveDuplicateJob`, `savedJobResponse_containsSavedJobIdAndJobId`.

**Auth**: `register_success`, `register_duplicateUsername_returns400`, `login_disabledUser_returns401`, `jwt_disabledUser_returns401`, `forgotPassword_doesNotRevealUnknownEmail`, `resetPassword_invalidToken_returns400`.

**Admin**: `admin_canApproveCompanyApplication`, `admin_canRejectCompanyApplicationWithReason`, `nonAdmin_cannotApproveCompanyApplication`, `admin_canDisableUser`, `admin_canDisableCompany`, `disabledCompany_losesWriteAccess`.

### 12.2 Migration tests with Testcontainers

Add `DatabaseMigrationIT`: fresh MySQL + Flyway migrations + Spring context must pass. Do not rely on H2 for production schema confidence.

### 12.3 Contract tests

Snapshot frontend-critical DTOs: `LoginResponse`, `SessionResponse`, `JobCardResponse`, `SavedJobResponse`, `JobApplicationResponse`, `ApiError`. Fail CI if their shape changes accidentally.

### 12.4 Coverage target

Do not chase fake line coverage. Target: full coverage of guards, high service coverage, integration coverage of controllers and repositories.

---

## Phase 13 — Observability and operational safety

- Replace `ex.printStackTrace()` in `GlobalExceptionHandler` with `Logger`.
- Add `RequestIdFilter` — every response includes `X-Request-Id`, every `ApiError` includes `requestId`.
- Track metrics: login attempts, registrations, job applications, jobs published, password resets, API latency, error rates, DB pool usage.
- Document backup, restore, and migration rollback procedures.

---

## Phase 14 — Performance and scale

- Use a `PublicJobCardProjection` for list queries instead of loading full entity graphs.
- Create separate DTOs: `PublicJobCardResponse`, `PublicJobDetailResponse`, `CompanyJobRowResponse`, `AdminJobRowResponse`.
- Fix `Job.incrementApplicants()` drift — either query count (`COUNT(*) WHERE status != 'WITHDRAWN'`) or maintain a robust denormalized counter.
- Add indexes for common filter/sort columns on `jobs`, `job_applications`, `saved_jobs`, `company_applications` once traffic exists.

---

## Phase 15 — Product polish and serious differentiators

Build only after the foundation is solid.

- **Candidate recommendations** — rule-based, based on degree field, location, saved jobs, experience.
- **Resume parsing** — `POST /api/users/me/profile/resume/parse`, extract education/skills/experience, user confirms before profile update.
- **Skills taxonomy** — `skills`, `candidate_skills`, `job_required_skills`, `job_preferred_skills`.
- **Messaging** — only after moderation is strong; company must have an active application to message candidate.
- **Interview scheduling** — `interviews(application_id, scheduled_at, location_or_meeting_url, status)`.
- **Company subscriptions** — free tier / paid tier / featured jobs / candidate search access — only after core is reliable.

---

## Backend package roadmap

Current structure (technical layers) is fine for now. Target (feature-based):

```
com.secure.jobs
  auth / users / companies / jobs / applications / savedjobs
  profiles / admin / uploads / notifications / audit
  common (exceptions, security, pagination, validation)
```

Do not refactor prematurely — wait until Phase 0 bugs and migrations are fixed.

---

## Next.js frontend roadmap

Build the frontend only after Phases 0–3 are stable.

**Module order**: Public site → Candidate dashboard → Company onboarding → Company dashboard → Admin dashboard.

Do not start the frontend before:
- Flyway baseline is production-real
- `ddl-auto=validate` is set
- Hardcoded admin is removed
- JWT disabled-user check is in place
- CORS is configured
- DTO IDs are stable

---

## Milestone summary

| Milestone | Exit criteria |
|---|---|
| 1 — Stabilized backend | Fresh DB boots from migrations; company isolation holds; disabled users rejected; stable IDs |
| 2 — Frontend-ready API | OpenAPI live; typed error codes; enum/options/session endpoints; no ambiguous IDs |
| 3 — Candidate MVP | Candidate can register, profile, upload resume, search, apply, save, track status |
| 4 — Company MVP | Approved company can manage jobs and applicants without cross-company data leaks |
| 5 — Admin MVP | Admin can operate the platform without direct DB access |
| 6 — Production readiness | Docker Compose, CI, Testcontainers, Actuator, structured logs, rate limiting, refresh tokens, email verification |
| 7 — Serious platform | Company teams, employer notes, candidate experience/certs, notifications, job alerts, SEO, better search |

---

## What not to build yet

Do not build until the foundation is solid: AI resume matching, payments, chat/messaging, complex recommendations, multi-tenant billing, mobile app, advanced analytics.

---

## The brutal priority list

If only ten things get done next, do these:

1. Fix `JobServiceImpl.updateJob()` guard argument order.
2. Fix `JobServiceImpl.deleteJob()` guard argument order.
3. Fix `SavedJobMapper.toResponse()` — rename `id` to `savedJobId` and expose `jobId` separately.
4. Replace fake Flyway baseline with a real clean-database schema.
5. Remove hardcoded `superAdmin/superPass` from `DataInitializer`.
6. Add disabled/locked account checks inside `JwtAuthenticationFilter`.
7. Add DB unique constraints for username, email, and role name.
8. Add request validation to `CreateJobRequest`, `UpdateJobRequest`, and profile/company DTOs.
9. Add OpenAPI, stable error codes, and normalized DTO IDs.
10. Add integration tests that would have caught the current bugs.
