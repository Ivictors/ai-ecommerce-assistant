# Tasks — MVP Account Provisioning

Implementation policy: complete and validate each task in order, then create
one focused Conventional Commit for that task. Do not combine tasks into one
commit; work remains on `main` as agreed for this solo project.

## T-001: Migrate test fixtures and require password hashes

- AC-IDs: AC-007
- Test-IDs:
  - T-001-T1 — clean database migrates through V7–V9 and retains product fixtures
  - T-001-T2 — unrelated users and dependent records survive targeted cleanup
  - T-001-T3 — database rejects a null password hash after V8
- Files in scope:
  - `src/main/resources/db/migration/V7__remove_seeded_account_fixtures.sql` (new)
  - `src/main/resources/db/migration/V8__require_user_password_hash.sql` (new)
  - `src/main/resources/db/migration/V9__create_account_provisioning_audit.sql` (new)
  - `src/test/java/com/victor/ecommerce/presentation/rest/conversation/ConversationCreationIntegrationTest.java`
  - `src/test/java/com/victor/ecommerce/infrastructure/persistence/account/AccountProvisioningMigrationIntegrationTest.java` (new)
- Dependencies: none
- Gates: PostgreSQL migration integration, Hibernate schema validation, existing backend tests
- Rollback: migrations are forward-only; restore a backup for unintended data deletion. Do not edit V3/V6.
- Notes: exact fixture emails only; delete conversations/order items/orders before users. Keep products.

## T-002: Implement the atomic account-bootstrap use case

- AC-IDs: AC-001, AC-002, AC-003, AC-005, AC-010
- Test-IDs:
  - T-002-T1 — ADMIN-only creation delegates hashing and persists ADMIN role
  - T-002-T2 — requested test USER is created with USER role in the same use case
  - T-002-T3 — existing ADMIN prevents account creation
  - T-002-T4 — invalid/duplicate details create no partial accounts
  - T-002-T5 — hash/persistence failure rolls back every requested account
  - T-002-T6 — generated password hash verifies with the approved Argon2id hasher
- Files in scope:
  - `src/main/java/com/victor/ecommerce/application/account/AccountBootstrapService.java` (new)
  - `src/main/java/com/victor/ecommerce/application/account/AccountBootstrapTransaction.java` (new)
  - `src/main/java/com/victor/ecommerce/application/account/UserAccountRepository.java` (new)
  - `src/main/java/com/victor/ecommerce/application/account/AccountProvisioningAudit.java` (new)
  - `src/main/java/com/victor/ecommerce/domain/user/User.java`
  - `src/test/java/com/victor/ecommerce/application/account/AccountBootstrapServiceTest.java` (new)
  - `src/test/java/com/victor/ecommerce/domain/user/UserTest.java` (new)
- Dependencies: T-001
- Gates: unit tests, domain invariant tests, authentication regression tests
- Rollback: revert the focused commit; preserve forward migrations and follow with a corrective migration if schema has already been applied.
- Notes: keep role choice internal; do not accept arbitrary role strings from the CLI.

## T-003: Add PostgreSQL persistence and audit adapters

- AC-IDs: AC-001, AC-002, AC-005, AC-011
- Test-IDs:
  - T-003-T1 — advisory transaction lock serializes bootstrap attempts
  - T-003-T2 — success audit commits with account creation
  - T-003-T3 — failure audit persists after account transaction rollback
  - T-003-T4 — audit failure falls back to a safe structured log
  - T-003-T5 — audit rows contain no names, emails, passwords, or hashes
- Files in scope:
  - `src/main/java/com/victor/ecommerce/infrastructure/persistence/user/UserRepository.java`
  - `src/main/java/com/victor/ecommerce/infrastructure/persistence/account/AccountProvisioningAuditRecord.java` (new)
  - `src/main/java/com/victor/ecommerce/infrastructure/persistence/account/AccountProvisioningAuditRepository.java` (new)
  - `src/main/java/com/victor/ecommerce/infrastructure/persistence/account/PostgresAccountProvisioningAudit.java` (new)
  - `src/test/java/com/victor/ecommerce/infrastructure/persistence/account/AccountBootstrapPersistenceTest.java` (new)
- Dependencies: T-001, T-002
- Gates: PostgreSQL integration tests, transaction/rollback tests, no-secret log assertions
- Rollback: revert the focused adapter commit; retain the additive audit schema unless a later forward migration removes it.
- Notes: use PostgreSQL transaction-level advisory locking; no new persistence framework or logging platform.

## T-004: Add the secure operator command

- AC-IDs: AC-001, AC-002, AC-003, AC-004, AC-005, AC-008, AC-010, AC-011
- Test-IDs:
  - T-004-T1 — CLI accepts only the optional test-user flag
  - T-004-T2 — hidden password prompts confirm matching values and clear buffers
  - T-004-T3 — no console and mismatched confirmation fail before writes
  - T-004-T4 — bootstrap entry point does not open HTTP or invoke OpenAI
  - T-004-T5 — safe output and stable exit codes for success and failure
- Files in scope:
  - `src/main/java/com/victor/ecommerce/presentation/cli/AccountBootstrapCommand.java` (new)
  - `src/main/java/com/victor/ecommerce/presentation/cli/ConsolePasswordPrompt.java` (new)
  - `src/main/java/com/victor/ecommerce/presentation/cli/BootstrapMain.java` (new)
  - `src/main/resources/application-bootstrap.properties` (new)
  - `pom.xml`
  - `src/test/java/com/victor/ecommerce/presentation/cli/AccountBootstrapCommandTest.java` (new)
  - `src/test/java/com/victor/ecommerce/presentation/cli/BootstrapMainTest.java` (new)
- Dependencies: T-002, T-003
- Gates: unit tests, Quarkus command-mode tests on pinned 3.39.3, verify no listening socket/provider call
- Rollback: revert the focused command commit; normal HTTP application entry point remains the default.
- Notes: fail closed if `System.console()` is unavailable; no Picocli dependency is required for the single optional flag.

## T-005: Verify the complete bootstrap and existing auth flow

- AC-001 through AC-011
- Test-IDs:
  - T-005-T1 — clean-install bootstrap creates ADMIN and optional test USER
  - T-005-T2 — both accounts authenticate through `POST /api/auth/login`
  - T-005-T3 — issued role claims preserve existing ADMIN/USER authorization
  - T-005-T4 — concurrent invocations yield one initial ADMIN
  - T-005-T5 — captured output/logs/database contain no raw secret or hash
  - T-005-T6 — clean Flyway validation plus full backend test suite
- Files in scope:
  - `src/test/java/com/victor/ecommerce/presentation/cli/AccountBootstrapIntegrationTest.java` (new)
  - `src/test/java/com/victor/ecommerce/presentation/rest/auth/AuthenticationIntegrationTest.java`
  - `src/test/java/com/victor/ecommerce/presentation/rest/conversation/ConversationCreationIntegrationTest.java`
  - `.specs/account-provisioning/05-implementation-log.md` (created during RED/GREEN)
  - `.specs/account-provisioning/06-validation-report.md` (created at verification)
- Dependencies: T-001, T-002, T-003, T-004
- Gates: `mvnw test`, clean database migration, full `mvnw clean verify`, security review, traceability review
- Rollback: revert the focused test/documentation commit; no production data rollback is performed by this verification task.
- Notes: use the repository's PostgreSQL/pgvector test setup; no OpenAI or production credentials.

## Dependency Order and Parallelism

```text
T-001 schema/invariant
   ↓
T-002 application use case
   ↓
T-003 PostgreSQL and audit adapters
   ↓
T-004 operator command
   ↓
T-005 full workflow verification
```

These tasks are intentionally sequential: the schema and account invariant
constrain the application use case; persistence and auditing support its
transaction; the CLI depends on those services; final verification covers the
integrated path. No task should be implemented in parallel for this solo
workflow.

## AC-to-Task Traceability

| Acceptance criterion | Tasks |
| --- | --- |
| AC-001 | T-002, T-003, T-004, T-005 |
| AC-002 | T-002, T-003, T-004, T-005 |
| AC-003 | T-002, T-004, T-005 |
| AC-004 | T-004, T-005 |
| AC-005 | T-002, T-003, T-004, T-005 |
| AC-006 | T-005 |
| AC-007 | T-001, T-005 |
| AC-008 | T-004, T-005 |
| AC-009 | T-005 |
| AC-010 | T-002, T-004, T-005 |
| AC-011 | T-003, T-004, T-005 |
