# High-Level Technical Design — MVP Account Provisioning

## Architecture Diagram

```text
Operator (interactive terminal)
  | non-secret command option + hidden password prompts
  v
Quarkus command-mode entry point (HTTP listener disabled)
  |
  v
AccountBootstrapService [application, transaction boundary]
  |---------------------> PasswordHasher port -> Argon2PasswordHasher
  |---------------------> Account repository port -> PostgreSQL
  |---------------------> ProvisioningAudit port -> PostgreSQL audit table + safe log fallback
  v
User domain account (USER or ADMIN; password hash required)

Flyway V7: remove only designated seeded account/order/conversation fixtures
Flyway V8: enforce users.password_hash NOT NULL

Existing runtime remains unchanged:
Angular -> POST /api/auth/login -> existing JWT issuer -> protected REST APIs
```

The command runs after Quarkus and Flyway initialization but is packaged or
selected separately from the normal HTTP entry point. The bootstrap runtime
must not open an HTTP listener or make AI-provider calls. Quarkus supports
  command-mode entry points and named main selection; the exact build profile
  must be verified against the repository's pinned Quarkus version during
  low-level design and implementation ([Quarkus command-mode guide](https://quarkus.io/guides/command-mode-reference/)).
  The CLI configuration must disable HTTP hosting and OpenAI calls using
  supported Quarkus/LangChain4j configuration ([Quarkus configuration
  reference](https://quarkus.io/guides/all-config/), [LangChain4j integration
  controls](https://docs.quarkiverse.io/quarkus-langchain4j/dev/enable-disable-integrations.html)).
The CLI configuration must disable HTTP hosting using supported Quarkus HTTP
configuration and disable OpenAI calls using the LangChain4j integration
switch ([Quarkus configuration reference](https://quarkus.io/guides/all-config/),
[Quarkus LangChain4j integration controls](https://docs.quarkiverse.io/quarkus-langchain4j/dev/enable-disable-integrations.html)).

## Component Responsibilities

| Component | Responsibility |
| --- | --- |
| `presentation/cli` bootstrap command | Parse only non-secret options, require an interactive terminal, collect account details, read passwords without echo, display safe outcome, and return a process exit code. |
| `application` bootstrap use case | Validate account details, coordinate hashing and persistence, prevent a second bootstrap ADMIN, and own the transaction boundary. |
| `domain/user` | Represent an application account with a role and mandatory password hash; account construction must not permit a missing hash. |
| Account repository port/adapter | Persist accounts and perform the serialized check for an existing ADMIN using PostgreSQL. |
| Existing `PasswordHasher` / `Argon2PasswordHasher` | Hash passwords with the already configured Argon2id parameters; do not introduce another password algorithm. |
| Provisioning audit port/adapter | Record successful and failed attempts in PostgreSQL with safe structured-log fallback, without passwords, hashes, or other bootstrap secrets. |
| Flyway migrations | Remove only the named V3 test-account fixtures and dependent test records, then enforce the non-null hash invariant in a later forward migration. |

No REST resource or anonymous HTTP bootstrap operation is added. The CLI is an
adapter to an application use case; it does not write through directly to
Panache or SQL.

## API Contract Sketch

This feature has no HTTP API contract. The interface is an operator command:

```text
account-bootstrap [--include-test-user]
```

- The optional flag requests creation of one test `USER` in addition to the
  first `ADMIN`.
- Names, email addresses, and passwords are entered interactively; passwords
  are read without echo and are never accepted as command-line arguments or
  environment variables.
- If the process has no interactive console, it exits unsuccessfully before
  changing account data.
- Success output confirms completion without printing passwords or hashes.
- Failure output gives a safe reason and non-zero exit status without exposing
  credentials or database internals.
- The exact exit-code mapping and input validation messages are low-level
  design details.

## Data Flow

1. The operator starts the dedicated bootstrap entry point. Quarkus initializes
   configuration, the datasource, and Flyway; HTTP hosting and AI-provider
   integration are disabled for this execution mode.
2. The command verifies that a real interactive console is available and
   gathers the ADMIN identity and hidden password (and, if requested, the test
   USER identity and hidden password).
3. The command passes the values to the application use case. Validation
   rejects incomplete input and duplicate email addresses before persistence.
4. The use case serializes bootstrap attempts, checks that no ADMIN already
   exists, hashes passwords through the existing Argon2id port, and stores the
   requested account(s).
5. The use case records success or failure through the audit boundary, without
   secret values. Successful audit and account rows commit together; failed
   outcomes are recorded after the account transaction rolls back, with a safe
   structured-log fallback if audit persistence fails.
6. The CLI returns a safe outcome and exits. Users authenticate later through
   the existing login and JWT implementation.

The transaction behavior is all-or-nothing: when the optional test USER is
requested, the ADMIN and USER are committed together; an error leaves neither
account created. A database-level serialization mechanism prevents two
concurrent invocations from both passing the no-ADMIN check. The product owner
approved this behavior on 2026-10-07.

## Data Model Overview

- `users.password_hash` remains the existing credential column and becomes
  `NOT NULL` in a new forward migration.
- `User` remains the application identity for login, role authorization, JWT
  subject, orders, and conversations. A guest/purchaser identity is not added.
- `User` account creation must require name, unique email, password hash, and
  persisted role (`ADMIN` or `USER`); its current constructor that creates a
  passwordless `USER` must be replaced or constrained before the database
  invariant is enforced.
- V7 removes only the three exact seeded user fixtures from V3 and their
  dependent conversations, order items, and orders in foreign-key-safe order.
  Product fixtures remain untouched by this account-provisioning scope.
- V8 applies `ALTER COLUMN password_hash SET NOT NULL` after the designated
  passwordless fixtures are removed. V3 and V6 remain immutable.
- Existing tests that insert users without `password_hash` must be updated to
  provide a test hash before the constraint is applied.
- If an audit table is approved, its model and migration are added to this
  design before low-level task decomposition; no audit schema is assumed yet.

## Security Posture

- Bootstrap authority is local/operator-controlled, not authenticated by an
  existing ADMIN and not exposed over HTTP.
- Passwords are acquired only through hidden interactive input. If the console
  is unavailable, the workflow fails closed rather than falling back to
  visible standard input, process arguments, or environment variables.
- Password values are short-lived in process memory, passed to the existing
  hasher, excluded from logs/exceptions/audit, and never persisted raw.
- Account creation and the no-existing-ADMIN check occur in one serialized
  transaction. A repeat or concurrent bootstrap cannot silently create a
  second bootstrap ADMIN.
- The command runtime disables HTTP hosting and OpenAI integration so running
  the operator tool does not expose API endpoints or invoke external AI
  services. The normal backend configuration remains unchanged.
- The migration deletes only explicitly named fixture emails, never all rows
  with a null hash. Before applying to any database containing non-test data,
  the operator must take a backup and verify that the named fixture identities
  have not been repurposed.
- Audit data contains outcome/reason and minimal operation metadata only; the
  exact fields and retention are specified in the low-level design.

## Observability Requirements

- Record both successful and failed bootstrap attempts, including a safe
  reason code, without recording passwords, hashes, or bootstrap secrets.
- Emit a concise operator-facing success/failure message and process status.
- Never include raw account input in exception text or logs unless the approved
  audit design explicitly justifies a minimal non-secret identifier.
- No metrics or alerting requirement is approved for this one-time local MVP
  operation.

## Key Trade-offs and Alternatives Considered

- **Dedicated operator command vs protected ADMIN endpoint:** the endpoint
  cannot authorize the first ADMIN because no ADMIN exists yet; an anonymous
  endpoint would create a remotely exploitable bootstrap path. The local
  command avoids that deadlock and attack surface.
- **Use Keycloak vs preserve local auth:** full Keycloak integration would
  replace token issuance/validation and identity mapping, exceeding the
  approved MVP. The project instead keeps its own login/JWT flow and borrows
  only the controlled bootstrap principle.
- **Hidden terminal input vs arguments/environment:** hidden input is suited
  to the approved interactive operator flow and avoids common command-line and
  configuration exposure. It is not suitable for unattended automation; that
  is outside the MVP.
- **Remove test users vs separate credential table:** removing the known
  passwordless fixtures and requiring a hash on every persisted application
  user is simpler for this MVP and matches the user's decision. A separate
  credential entity would preserve passwordless accounts but adds a lifecycle
  model not needed for these disposable fixtures.
- **Atomic bootstrap vs partial success:** all-or-nothing creation makes a
  requested ADMIN+test USER setup retryable and prevents a half-complete
  bootstrap. This is proposed in ADR-003 and requires approval.
- **Durable audit table vs structured logs:** a table provides queryable,
  database-backed audit history; structured logs require less schema but
  depend on log retention and can be unavailable when database connectivity
  fails. Recommendation: a minimal audit table plus a safe structured log
  fallback (ADR-004).

ADRs:

- [ADR-001 — Operator-only Quarkus command bootstrap](adr/ADR-001-operator-bootstrap-command.md)
- [ADR-002 — Forward migration cleanup and non-null credential](adr/ADR-002-non-null-password-hash.md)
- [ADR-003 — Atomic bootstrap transaction](adr/ADR-003-atomic-bootstrap.md)
- [ADR-004 — Bootstrap audit record](adr/ADR-004-bootstrap-audit.md)

## Risks and Rollback Strategy

- **Fixture deletion is destructive.** Mitigation: V7 predicates only on the
  three known fixture emails and dependent rows; test against both a clean
  database and a database containing unrelated accounts/orders/conversations.
  Back up any non-disposable database before applying the migration. Flyway
  migrations are forward-only; recovery from unintended deletion requires
  restoring the backup, not editing V3/V6 or silently recreating user data.
- **The CLI could start the HTTP listener or external integrations.** Mitigation:
  use a separately selected command entry point and CLI-specific configuration
  that disables HTTP hosting and OpenAI calls; verify no socket binds and no
  external request occurs in command integration tests.
- **Argon2id parameters increase command duration and memory use.** Reuse the
  existing configured hasher and do not duplicate parameters.
- **A failed optional USER creation could leave only an ADMIN.** Proposed
  mitigation is a single transaction (ADR-003).
- **The existing ORM model permits a null hash.** Update the domain/entity
  constructor and all test fixtures before enabling schema validation against
  the new constraint.
- **Audit availability and retention:** a database outage can prevent a durable
  audit record; write a safe structured fallback and revisit centralized log
  mapping before production operations.

## High-Level Test Scenario Map

| AC | Scenario families |
| --- | --- |
| AC-001, AC-002 | First ADMIN created; optional test USER created only when requested; resulting roles and account count. |
| AC-003, AC-008 | Both passwords stored as verifiable Argon2id hashes; raw values absent from database, output, captured logs, and audit. |
| AC-004, AC-009 | No HTTP bootstrap route; normal application login/JWT and role enforcement remain unchanged. |
| AC-005 | Existing ADMIN causes safe refusal with no account mutation; repeated invocation remains safe. |
| AC-006 | ADMIN and optional USER can each log in through existing REST flow and receive expected persisted role. |
| AC-007 | Migrations remove only exact test fixtures and dependent records; unrelated users and their orders/conversations survive; password hash constraint rejects null. |
| AC-010 | Missing input and duplicate email fail without partial account creation or secret disclosure. |
| AC-011 | Success and failure are auditable without secret fields; database/audit failure behavior is covered according to the approved audit design. |

Additional command scenarios: no interactive console, database unavailable,
two concurrent bootstrap attempts, transaction rollback, migrations on a clean
installation, and compatibility with test fixtures after `NOT NULL`.

## Open Questions

None at the high-level design gate. Low-level design must define audit fields
and retention without storing secrets or unnecessary personal data.

## Resolved Questions

- **Q-001 from intent spec:** How must the operator provide bootstrap
  passwords?
  - Resolution: the operator uses interactive hidden input; the CLI fails
    closed without an interactive console and never accepts passwords as
    arguments or environment variables.
  - Resolved at: 2026-10-07 by user approval.
- **Q-002 from intent spec:** Should initial application authentication move to
  Keycloak?
  - Resolution: no; preserve the existing application login/JWT architecture
    and use only an operator-controlled bootstrap pattern.
  - Resolved at: 2026-10-07.
- **Q-003 from intent spec:** How should passwordless seeded users be handled?
  - Resolution: remove designated test users and related sample records via
    forward migrations; do not change V3 or V6 and do not delete by a broad
    predicate.
  - Resolved at: 2026-10-07.
- **Q-004 from intent spec:** Should bootstrap optionally create a test
  customer?
  - Resolution: yes, one test `USER` may be created during the same operator
    bootstrap operation.
  - Resolved at: 2026-10-07.
- **Q-005 from intent spec:** Must every persisted application user have a
  password hash?
  - Resolution: yes, the final schema requires a non-null hash.
  - Resolved at: 2026-10-07.
- **Q-006:** Where should bootstrap success/failure be audited?
  - Resolution: “yes, approved. how it is a MVP we can use it. but in future
    we will use a tool to map logs.” Use a minimal PostgreSQL audit table with
    safe structured-log fallback. A future log-mapping/analysis tool is
    deferred and not part of this MVP.
  - Resolved at: 2026-10-07.
- **Q-007:** Should creation of ADMIN and the optional test USER be atomic and
  serialized against concurrent bootstrap attempts?
  - Resolution: “yes, approved.” Create all requested accounts in one
  transaction and prevent concurrent bootstrap operations from both
  succeeding.
  - Resolved at: 2026-10-07.
- **Q-008:** Should the project upgrade Quarkus before completing this MVP?
  - Resolution: Keep the current Quarkus 3.39.3 and Java 21 for this MVP; do not include an
    upgrade in #44. Issue #48 is the final technical compatibility gate in
    the MVP: after #14 and before the final review in #15, update to the then-
    current supported LTS and confirm the selected JDK distribution is
    supported.
  - Resolved at: 2026-10-07.
