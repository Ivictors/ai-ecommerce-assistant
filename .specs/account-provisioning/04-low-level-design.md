# Low-Level Design — MVP Account Provisioning

## API Contracts

This feature exposes no HTTP endpoint. The operator interface is a command:

```text
account-bootstrap [--include-test-user]
```

| Input/output | Contract |
| --- | --- |
| `--include-test-user` | Optional, non-secret flag; requests one test `USER`. No other command-line account values or secrets are accepted. |
| ADMIN details | Prompt for name and email; prompt twice for password using hidden terminal input. |
| Test USER details | If the flag is present, prompt for name and email; prompt twice for password using hidden terminal input. |
| No interactive console | Fail closed before creating accounts; do not fall back to visible input. |
| Success | Exit code `0`; print a generic completion message without password, hash, or unnecessary personal data. |
| Usage/input failure | Exit code `2`; print a safe validation/usage reason to stderr. No accounts are created. |
| Bootstrap/runtime failure | Exit code `1`; print a safe error code, not SQL, stack trace, password, or hash. |

Safe failure reason codes: `NO_INTERACTIVE_CONSOLE`, `INVALID_INPUT`,
`PASSWORD_MISMATCH`, `ADMIN_ALREADY_EXISTS`, `DUPLICATE_EMAIL`,
`DATABASE_FAILURE`, and `AUDIT_FAILURE`. The process output may include a
correlation/attempt identifier, but no raw password, hash, or secret.

### T-002 application contract

The use case is exposed by `com.victor.ecommerce.application.account.AccountBootstrapService`:

```java
BootstrapResult bootstrap(BootstrapRequest request)
```

The immutable input/output records are nested in `AccountBootstrapService`:

```java
record AccountDetails(String name, String email, String password)
record BootstrapRequest(AccountDetails admin, Optional<AccountDetails> testUser)
record BootstrapResult(long adminUserId, OptionalLong testUserId)
```

`AccountDetails.toString()` must redact its password instead of using the
record-generated representation.

The request always includes ADMIN details. `testUser` is empty unless the
operator explicitly requested a test account. Role is not caller-controlled:
the service assigns `ADMIN` to the first account and `USER` to the optional
account. Passwords are transient inputs and must never appear in `toString`,
logs, exception messages, audit records, or results.

The use case coordinates these application ports:

```java
interface AccountBootstrapTransaction {
    <T> T execute(Supplier<T> work);
}

interface UserAccountRepository {
    boolean administratorExists();
    boolean emailExists(String email);
    long persist(User user);
}

interface AccountProvisioningAudit {
    void recordSuccess(boolean testUserRequested, long adminUserId, Long testUserId);
    void recordFailure(BootstrapFailureCode reason, boolean testUserRequested);
}
```

The T-002 service is implemented as a directly constructible application
service and is not registered as a CDI bean until T-003 supplies CDI
implementations for all three ports. T-003 then adds the service bean scope
and transaction adapter together, avoiding an intermediate Quarkus runtime
with unsatisfied CDI dependencies.

`AccountBootstrapTransaction.execute` runs the callback in one database
transaction and propagates failures. It does not write audit records or
translate exceptions. The service calls `recordSuccess` inside the callback,
so a success-audit failure rolls back the accounts. After `execute` returns
exceptionally, the service calls `recordFailure` outside the rolled-back
transaction and rethrows a safe bootstrap exception. The persistence adapter
must run failure-audit writes in an independent transaction (T-003).

`administratorExists` and email pre-checks occur while the serialization lock
is held by the transaction adapter. The database unique constraint remains
authoritative for concurrent or collation-level email conflicts; T-003 maps a
unique-constraint conflict to `DUPLICATE_EMAIL`. The repository persists a
supplied domain `User` and returns its generated ID; it does not choose roles
or hash passwords.

The service uses the existing `PasswordHasher` port to hash each password and
constructs a `User` with the encoded hash and explicit role. Raw passwords
must not be passed to repository or audit ports.

`BootstrapFailureCode` is a safe enum containing `INVALID_INPUT`,
`ADMIN_ALREADY_EXISTS`, `DUPLICATE_EMAIL`, `BOOTSTRAP_FAILURE`, and
`AUDIT_FAILURE`.
`AccountBootstrapException.reasonCode()` exposes only that code and must not
retain raw input or an exception message. The CLI maps infrastructure failure
to the established `DATABASE_FAILURE` output. Failure-audit exceptions do not
replace the original bootstrap failure; safe logging fallback is T-003.

Validation requires a non-null request and ADMIN details; optional USER
details, when present, are also required fields. Name and password must be
nonblank; name must fit the 150-character column; email must have valid syntax
and fit the 255-character column; emails within one request must differ.
Invalid values fail as `INVALID_INPUT` without persisting accounts. The
service accepts no role parameter.

### T-003 PostgreSQL adapter contract

`PostgresUserAccountRepository` implements `UserAccountRepository` through
composition with the existing Panache `UserRepository`. It must not extend or
implement `PanacheRepository`: the application's `long persist(User)` conflicts
with Panache's `void persist(User)`. The adapter delegates persistence, flushes
within the account transaction to surface constraint failures, and returns
the generated ID. Administrator existence
is determined from `users.role = 'ADMIN'`; email existence is checked by exact
stored email; persistence uses the existing `User` entity and returns its
generated ID. The database unique constraint remains authoritative, and a
unique-constraint violation is translated to `DUPLICATE_EMAIL` without
including SQL or provider messages.

`PostgresAccountBootstrapTransaction.execute(Supplier<T>)` is a CDI bean with
a JTA transaction around the entire callback. Before invoking the callback it
acquires one transaction-scoped PostgreSQL advisory lock using a stable
64-bit key dedicated to this bootstrap operation. The lock, ADMIN check, all
account inserts, and success audit therefore share the same transaction. The
adapter propagates callback failures so JTA rolls back.

`AccountProvisioningAuditRecord` maps only the seven V9 columns: `id`,
`attempted_at`, `outcome`, `reason_code`, `test_user_requested`,
`admin_user_id`, and `test_user_id`. `AccountProvisioningAuditRepository`
persists that record. `PostgresAccountProvisioningAudit.recordSuccess` joins
the current account transaction; failure must propagate so account creation
rolls back. `recordFailure` runs in a new transaction after the account
transaction has ended. Audit storage errors emit a structured safe log event
with the outcome/reason only; never include exception text, identity data,
passwords, or hashes. A success-write failure propagates as `AUDIT_FAILURE`;
a failure-write error is logged and does not replace the original bootstrap
failure.

The T-003 persistence integration tests run against a UUID-scoped PostgreSQL
schema migrated through V1-V9. Test setup may create/drop only that schema and
must not mutate `public` application rows.

## Data Models

### Application user

`users` remains the identity table used by login, roles, JWT subjects, orders,
and conversations. After migration V8:

| Field | Rule |
| --- | --- |
| `id` | Existing generated primary key. |
| `name` | Required, maximum 150 characters. |
| `email` | Required, unique, maximum 255 characters; validate as an email address before persistence. |
| `password_hash` | Required, non-null Argon2id encoded hash. |
| `role` | Required existing enum value, `USER` or `ADMIN`. |
| `created_at` | Existing required timestamp. |

Domain account creation must require a nonblank hash and explicit role. No
constructor or factory may create an application user without credentials.
The T-002 domain constructor is `User(String name, String email,
String passwordHash, UserRole role)`. It rejects null/blank name, email, or
hash and null role; email syntax is validated by the application service. It
initializes `created_at` as today. The old passwordless constructor is removed
so production code cannot create an application account without credentials.

### Bootstrap audit record

V9 creates `account_provisioning_audit`:

| Field | Rule |
| --- | --- |
| `id` | Generated primary key. |
| `attempted_at` | Required timestamp, database-generated. |
| `outcome` | Required `SUCCESS` or `FAILURE`, enforced by a check constraint. |
| `reason_code` | Required bounded safe code; no exception message or user input. |
| `test_user_requested` | Required boolean. |
| `admin_user_id` | Nullable numeric historical identifier, set only on success. No FK so the audit remains if an account is later removed. |
| `test_user_id` | Nullable numeric historical identifier, set only when a requested test account was created. No FK. |

No name, email, password, password hash, prompt text, or raw exception is
stored in the audit record. Audit retention is not automated in this MVP; it
must be reviewed before official delivery.

### Flyway migration sequence

- V7 deletes conversations for the exact fixture emails
  `victor@example.com`, `ana@example.com`, and `carlos@example.com`; then their
  order items, orders, and users, in foreign-key-safe order. Products remain.
- V8 changes `users.password_hash` to `NOT NULL` after V7 removes the
  passwordless fixtures.
- V9 creates `account_provisioning_audit` and its constraints/index if needed.
- V3 and V6 remain unchanged. No broad `WHERE password_hash IS NULL` deletion
  is allowed.
- Integration tests and SQL fixtures must insert non-null password hashes
  after V8.

## Error Model

| Condition | Behavior | AC |
| --- | --- | --- |
| Missing terminal or redirected/no-console execution | Refuse before account transaction; safe message and exit `2`. | AC-008 |
| Blank/malformed name or email, blank password, password confirmation mismatch | Refuse with `INVALID_INPUT` or `PASSWORD_MISMATCH`; no account changes; record safe failure. | AC-008, AC-010, AC-011 |
| Email already exists | Roll back the requested bootstrap; report `DUPLICATE_EMAIL`; no partial account creation. | AC-010 |
| An ADMIN already exists | Do not create another bootstrap ADMIN; report `ADMIN_ALREADY_EXISTS`; audit the failed attempt. | AC-005, AC-011 |
| Concurrent bootstrap invocation | PostgreSQL transaction lock serializes requests; exactly one may pass the no-ADMIN check. Loser follows existing-admin failure. | AC-005, AC-011 |
| Password hashing or account persistence fails | Roll back all requested accounts; safe `DATABASE_FAILURE`/`BOOTSTRAP_FAILURE`; persist failure audit separately when possible and log a safe fallback. | AC-001–AC-003, AC-008, AC-011 |
| Success audit write fails | Roll back accounts because success and audit must commit together; attempt a safe failure audit/log fallback. | AC-011 |
| Failure audit write fails | Preserve the original bootstrap failure; emit a safe structured log fallback; never expose secrets. | AC-011 |
| Login with a newly provisioned account | Existing login behavior, token claims, and role enforcement apply unchanged. | AC-006, AC-009 |
| Migration target contains unrelated users/orders/conversations | Preserve them; the fixture deletion must match only the exact designated emails. | AC-007 |

Error text must not include SQL, full exception messages, passwords, hashes,
or private keys. Exit codes remain stable for shell automation and operator
troubleshooting.

## Security Details

- `System.console()` (or an equivalent verified TTY facility) is mandatory;
  `Console.readPassword()` reads without echo. If no console exists, exit
  before entering credentials or writing accounts.
- Ask for each password twice and compare before hashing. On mismatch, clear
  both input buffers and make no database change.
- Clear password `char[]` buffers in `finally` blocks after hashing. The
  existing `PasswordHasher` API accepts `String`; minimize copies and never
  retain the raw value beyond the hash call.
- Passwords are not accepted from command-line arguments or environment
  variables. CLI options contain no secret.
- The application service validates required fields and email syntax; database
  uniqueness remains authoritative for races.
- A PostgreSQL transaction-level advisory lock is acquired before checking for
  an existing ADMIN. The lock, ADMIN check, account inserts, and success audit
  share one transaction.
- When the requested test USER cannot be created, roll back the ADMIN too.
  Failure audit uses a separate transaction after rollback; structured logging
  is a safe fallback.
- Bootstrap execution uses a separate Quarkus command entry point/configuration
  with HTTP host listening disabled and OpenAI integration disabled. Test that
  neither a socket nor an outbound provider call is opened.
- The command-mode entry point activates a CDI request context before invoking
  Panache-backed repositories; Quarkus command mode does not activate request
  scope by default ([Quarkus command-mode guide](https://quarkus.io/guides/command-mode-reference/)).
- Only `ADMIN` and `USER` roles can be created by the bootstrap use case; role
  is not accepted as arbitrary user input.
- Migrations are destructive only for the named seed identities and their
  dependent sample records. Back up any non-disposable database before
  migration.

## Test Strategy

- Unit tests: password confirmation/clearing, field validation, use-case
  orchestration, no-existing-ADMIN guard, role assignment, and safe error
  mapping.
- Persistence integration tests against PostgreSQL: migration effects,
  uniqueness, non-null constraint, advisory-lock serialization, transaction
  rollback, and audit transactions.
- Command-mode tests: argument parsing, hidden prompt adapter, no-console
  failure, process exit codes, and no HTTP listener in bootstrap mode.
- End-to-end integration tests: bootstrap ADMIN and optional USER, then log
  both in through `POST /api/auth/login`; prove persisted role and protected
  endpoint behavior.
- Security assertions: capture stdout/stderr/logs and inspect database rows to
  prove no raw password/hash/secret is disclosed or persisted.
- Use the existing PostgreSQL/pgvector local test setup; do not substitute H2
  for the migration/locking tests.

## Test Scenario Catalog with Edge Cases

| Test ID | Scenario | Expected result |
| --- | --- | --- |
| T-001-T1 | Fresh Flyway database applies V1–V9. | Schema validates; no seeded users/orders/conversations remain; products remain; audit table exists; `password_hash` is non-nullable. |
| T-001-T2 | Migration runs with unrelated user, order, item, and conversation. | All unrelated records remain. |
| T-001-T3 | Insert `NULL` password hash after V8. | PostgreSQL rejects the row. |
| T-002-T1 | Valid ADMIN-only bootstrap use case. | One ADMIN persisted with Argon2id hash and success result. |
| T-002-T2 | Valid ADMIN + optional USER request. | Both accounts persisted with respective roles and hashes. |
| T-002-T3 | Existing ADMIN. | No new users persisted; safe failure outcome. |
| T-002-T4 | Blank/overlong required field, malformed email, duplicate email in the request, or email already persisted. | No partial records; `INVALID_INPUT` or `DUPLICATE_EMAIL`; failure audit is outside the account transaction. |
| T-002-T5 | Hashing/persistence exception. | Transaction rolls back all requested accounts. |
| T-002-T6 | Verify a generated hash with the existing configured Argon2id implementation. | The correct password matches; an incorrect password does not. |
| T-003-T1 | Concurrent bootstrap transactions; release after commit/rollback. | Advisory lock serializes callbacks; exactly one ADMIN succeeds and the loser sees an existing ADMIN. |
| T-003-T2 | Success audit commits with accounts; injected success-audit failure. | Both commit together, or all requested accounts roll back. |
| T-003-T3 | Failure audit after account transaction rollback. | Account rows are absent; failure audit persists independently. |
| T-003-T4 | Audit storage failure with sentinel secrets in the failure context. | Safe structured fallback is captured; original failure is preserved; no secrets or provider exception text leak. |
| T-003-T5 | Persisted audit rows and schema inspection. | Exactly the seven approved columns; correct flags/outcome/IDs; no personal data, passwords, or hashes. |
| T-003-T6 | Database email uniqueness violation. | Safe DUPLICATE_EMAIL, no SQL disclosure, and no partial account creation. |
| T-004-T1 | Optional test-user flag, unknown options, attempted password argument. | Only approved option accepted; unsafe usage refused without echoing secrets. |
| T-004-T2 | Hidden password prompt and confirmation; exceptional cleanup. | Input not echoed; matching values accepted; available char buffers cleared on all paths. |
| T-004-T3 | No console or mismatched confirmation. | No account writes; safe failure and exit 2. |
| T-004-T4 | Packaged bootstrap entry point. | No HTTP listener or OpenAI call; normal HTTP application still works. |
| T-004-T5 | Successful, invalid-input, and runtime-failure command execution. | Safe output and approved exit codes 0, 2, and 1. |
| T-005-T1 | Clean-install bootstrap with and without optional USER. | Correct account counts, roles, and credential hashes. |
| T-005-T2 | Both provisioned accounts log in through existing REST endpoint. | Valid JWT subjects and roles match persisted accounts. |
| T-005-T3 | Protected customer/admin operations with resulting tokens. | Existing authorization and ownership rules are preserved. |
| T-005-T4 | Concurrent operator invocations. | Exactly one initial ADMIN; safe loser outcome. |
| T-005-T5 | Captured output/logs and audit/account rows. | Plaintext passwords absent everywhere in persisted data/output; hashes only in users.password_hash, never logs/audit. |
| T-005-T6 | Clean Flyway migration and complete backend validation. | Test and clean verify commands pass with recorded traceability. |

## Dependency/Version Policy

- Java runtime/compiler remains Java 21 for this MVP; use a JDK 21 distribution
  receiving vendor/community security updates. Do not move to another Java line
  as part of #44.
- Quarkus remains pinned to 3.39.3 and the matching Quarkus and
  Quarkus-LangChain4j BOMs for this MVP, as explicitly approved by the product
  owner. Quarkus 3.39 maintenance ended 2026-09-30; this is a temporary MVP
  exception, not the final MVP baseline. Issue #48 is the final technical
  compatibility gate within MVP, after #14 and before #15: upgrade to the
  then-current supported LTS and run full validation (3.40.1 was current on
  2026-10-07; re-check when implementing #48) ([Quarkus release/support status](https://quarkus.io/releases/),
  [Quarkus 3.40 LTS announcement](https://quarkus.io/blog/quarkus-3-40-released/)).
- Quarkus extensions must stay aligned through the existing platform BOM;
  do not override individual Quarkus extension versions.
- Reuse `jargon2-api` and `jargon2-native-ri-backend` 1.1.1 and the existing
  Argon2id configuration. A dependency upgrade is not part of this issue.
- Command mode must use APIs available in the pinned Quarkus version. Validate
  named entry-point packaging, request-context activation for Panache, and
  command testing against Quarkus 3.39.3 before implementation.
- Any dependency/version change discovered necessary during implementation
  returns to this policy for review; do not silently update the BOM.

## Open Questions

None. Product decisions on audit, atomicity, account scope, and MVP version
baseline are approved and recorded in `03-design.md` and its ADRs.
