# Account Provisioning Validation Report

## Scope

This report closes T-001 through T-005 for the MVP account provisioning flow.
The implementation is intentionally an operator-only bootstrap command; normal
HTTP startup remains the default application entry point.

## Traceability

| Acceptance area | Evidence |
| --- | --- |
| Schema and credential invariants | T-001 migrations and domain tests |
| Atomic bootstrap and fixed roles | T-002 application tests and T-005 integration test |
| PostgreSQL transaction serialization | T-003 persistence tests for advisory locking, rollback, and duplicate email |
| Durable success/failure audit | T-003 persistence tests and V9 column allowlist |
| Safe logging and output | T-003 log assertions and T-004/T-005 command tests |
| Operator command boundary | T-004 command and Quarkus entry-point tests |
| Authentication and authorization compatibility | T-005 login/JWT assertions plus existing authentication and authorization integration tests |

## Validation gates

- Targeted T-003 persistence tests: 8 passed.
- Targeted T-004 command tests: 8 passed.
- Targeted T-005 integration tests: 2 passed.
- Full backend suite: 104 passed, 0 failed, 0 errors.
- `mvnw.cmd clean verify`: successful; 104 tests passed, no integration-test failures.
- Flyway V1–V9 validation: successful in isolated and public schemas.
- Packaged bootstrap profile: started without an HTTP listener and returned `INVALID_INPUT` when no console was available.
- `git diff --check`: clean before commit.

## Security review

- Passwords are never logged or printed.
- Password confirmation uses hidden console input and clears both input buffers.
- Audit fallback logs contain only outcome and fixed reason code.
- Account creation is serialized by a PostgreSQL transaction-level advisory lock.
- Success audit participates in the account transaction; failure audit uses an independent transaction.
- No OpenAI credential or production secret was added. The bootstrap profile uses a non-secret sentinel only to satisfy mandatory configuration while the provider is not invoked.

## Decision

GO for the MVP account provisioning scope. Production hardening items such as
refresh tokens, password recovery, rotation, and external identity management
remain outside this task and must be handled by their previously planned issues.
