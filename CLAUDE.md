# Job Portal Backend — Claude Code Instructions

## Project summary

This is a Spring Boot backend for a serious job portal, similar to Indeed.

Core roles:
- `ROLE_USER`: candidate/job seeker
- `ROLE_COMPANY`: approved company/employer
- `ROLE_SUPER_ADMIN`: platform admin

The product goal is not just CRUD. The backend should evolve into a reliable job portal with:
- Candidate profiles, resumes, saved jobs, applications, and status tracking
- Employer company profiles, job lifecycle, applicant pipeline, and hiring workflow
- Admin moderation for users, companies, jobs, degree fields, and company applications
- Strong auth, authorization, audit logs, validation, migrations, and tests

The detailed roadmap is in:

`docs/job-portal-roadmap.md`

Read that roadmap before making roadmap-related decisions.

---

## Environment direction

This project is local-first.

AWS/cloud infrastructure was removed. Do not add AWS, S3, SES, SQS, Secrets Manager, CloudFront, or cloud upload logic unless explicitly asked.

Local target:
- Spring Boot backend on localhost:8080
- Next.js frontend on localhost:3000
- Local MySQL
- Local MailHog/local SMTP if email is needed
- Local uploads folder only if upload code exists

---

## Docker direction

This project is not Dockerized right now.

Do not add Dockerfile, docker-compose.yml, ECS, EC2, RDS, AWS, or deployment infrastructure unless explicitly asked.

Current priority is local backend correctness and stability. Dockerization comes later after core backend issues, migrations, security, API contract cleanup, and tests are stable.

Local development target:
- Spring Boot runs directly from IDE/Maven on localhost:8080.
- MySQL may still run locally or in an existing local Docker Desktop MySQL container exposed on localhost:3307.
- Next.js frontend runs on localhost:3000.

---

## Current highest priorities

Do these before building new features:

1. Fix `JobServiceImpl.updateJob()` guard argument order.
2. Fix `JobServiceImpl.deleteJob()` guard argument order.
3. Fix `SavedJobMapper.toResponse()` so saved job row ID and job ID are not confused.
4. Replace fake/incomplete Flyway baseline with real clean-database migrations.
5. Remove hardcoded `superAdmin/superPass` from `DataInitializer`.
6. Add disabled/locked account checks inside `JwtAuthenticationFilter`.
7. Add DB unique constraints for username, email, and role name.
8. Add validation to job, profile, and company request DTOs.
9. Add OpenAPI, stable error codes, and normalized DTO IDs.
10. Add integration tests that catch ownership, auth, and DTO correctness bugs.

These priorities come from the project roadmap and should guide implementation order. 

---

## Architecture rules

Follow the existing architecture unless a task explicitly asks for refactoring.

General boundaries:
- Controllers handle HTTP request/response only.
- Services handle business logic.
- Guards handle authorization and ownership checks.
- Repositories handle database access only.
- Mappers convert entities to DTOs.
- DTOs are used for API responses. Do not expose JPA entities directly.

Security-sensitive checks should be centralized in guards, not scattered across controllers.

Do not bypass guards with direct repository calls unless the task explicitly requires it.

---

## API rules

Use clear DTO ID names:
- `userId`
- `companyId`
- `jobId`
- `applicationId`
- `savedJobId`
- `degreeFieldId`
- `companyApplicationId`

Avoid ambiguous fields named only `id` in API responses when the response contains more than one domain object.

Never expose JPA entities directly in API responses.

Error responses should eventually use stable machine-readable codes, not only English messages.

---

## Spring Boot rules

Use existing project conventions where possible.

For exceptions:
- Prefer `ApiException` with an appropriate `HttpStatus`.
- Do not throw raw `RuntimeException` for user-facing API failures.

For validation:
- Add Jakarta validation annotations to request DTOs.
- Validate non-null partial update fields.
- Reject blank strings after trimming when relevant.

For security:
- Never hardcode production credentials.
- Never assume application-level uniqueness checks are enough; add DB constraints.
- Disabled or locked users must not be able to continue using old JWTs.

---

## Database and migration rules

Production schema must come from Flyway migrations, not Hibernate auto-create/update.

Target production behavior:
- `spring.flyway.enabled=true`
- `spring.jpa.hibernate.ddl-auto=validate`

Do not depend on tests that disable Flyway to prove production schema correctness.

When changing entities, update migrations and tests.

---

## Test rules

Every bug fix should include a test that would have failed before the fix.

Prefer integration tests for:
- Authorization boundaries
- Company ownership isolation
- JWT disabled-user behavior
- Saved job ID correctness
- Application status transitions
- Flyway clean-database startup

Do not remove tests to make the build pass.

When making changes, run the relevant test command. If the full test suite is too slow, run the targeted tests first and explain what was run.

---

## Claude Code working rules

Before editing:
1. Inspect the relevant files.
2. State the minimal plan.
3. Make the smallest focused change.
4. Add or update tests.
5. Run relevant tests.
6. Summarize results.

Do not:
- Rewrite unrelated code.
- Rename packages broadly unless the task asks for it.
- Change public API behavior accidentally.
- Mix unrelated roadmap phases in one change.
- Add speculative features while fixing a bug.

After every task, report:
1. Files changed
2. What changed
3. Tests added or updated
4. Commands run
5. Any remaining risks or follow-up work