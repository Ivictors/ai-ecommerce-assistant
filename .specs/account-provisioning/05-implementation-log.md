# Implementation Log — MVP Account Provisioning

## T-001 — RED

- Test-IDs: T-001-T1, T-001-T2, T-001-T3
- AC-IDs: AC-007
- Tests written:
  - T-001-T1: `AccountProvisioningMigrationIntegrationTest.test_AC007_T001T1_cleanMigrationRemovesOnlySeedAccountsAndCreatesAuditSchema` — expects seeded records removed, product fixtures retained, audit schema present, and password hashes non-nullable.
  - T-001-T2: `AccountProvisioningMigrationIntegrationTest.test_AC007_T001T2_migrationPreservesUnrelatedUserAndDependentData` — expects designated seeds removed while an unrelated account, order, item, and conversation survive.
  - T-001-T3: `AccountProvisioningMigrationIntegrationTest.test_AC007_T001T3_databaseRejectsNullPasswordHashAfterMigration` — expects PostgreSQL to reject a null password hash.
- Test support updated: the conversation integration fixture now supplies a non-null test hash in preparation for V8.
- First setup attempt: 3 errors because Flyway included the non-empty `public` schema in its managed schemas; the test configuration was corrected to manage only its generated schema.
- RED run: `mvnw.cmd -Dtest=AccountProvisioningMigrationIntegrationTest test` — 3 tests, 3 assertion failures, 0 errors. T-001-T1 and T-001-T2 failed because all 3 seeded users remained; T-001-T3 failed because PostgreSQL accepted a null hash. Compilation and Quarkus/PostgreSQL startup succeeded.
- Phase: RED — expected behavior gaps confirmed.
- Timestamp: 2026-10-08T20:40:02Z

## T-001 — GREEN

- Test-IDs: T-001-T1, T-001-T2, T-001-T3
- AC-IDs: AC-007
- Implementation:
  - Added V7 to delete only the three approved seeded users and their conversations, order items, and orders, in foreign-key-safe order.
  - Added V8 to enforce `users.password_hash NOT NULL` after the exact seed cleanup.
  - Added V9 with the approved provisioning audit fields and outcome constraint; no foreign keys or speculative index were added.
  - Updated authentication/conversation integration fixtures to persist non-null hashes.
- Targeted run: `mvnw.cmd -Dtest=AccountProvisioningMigrationIntegrationTest test` — 3 passed, 0 failed, 0 errors.
- Full backend run: `mvnw.cmd test` — 72 passed, 0 failed, 0 errors.
- Hibernate schema validation and Flyway V1–V9 validation succeeded against PostgreSQL 16.15.
- Phase: GREEN.
- Timestamp: 2026-10-08T20:46:17Z

## T-001 — REFACTOR

- Test-IDs: T-001-T1, T-001-T2, T-001-T3
- AC-IDs: AC-007
- Simplification: migration integration tests use a separate PostgreSQL datasource and UUID-scoped schema so they cannot leak a temporary `search_path` into the Quarkus pool or alter user tables in `public`.
- Run result: full backend suite remains green (72 passed).
- Phase: REFACTOR.
- Timestamp: 2026-10-08T20:46:17Z

## T-001 — SIMPLIFY

- Test-IDs: T-001-T1, T-001-T2, T-001-T3
- AC-IDs: AC-007
- Review: kept the three forward migrations narrowly scoped; did not add an audit index or broaden deletion predicates without an approved query/business need.
- Validation: `git diff --check` clean; `mvnw.cmd test` — 72 passed, 0 failed, 0 errors.
- Phase: SIMPLIFY — complete.
- Timestamp: 2026-10-08T20:46:17Z

## T-002 — RED

- Test-IDs: T-002-T1, T-002-T2, T-002-T3, T-002-T4, T-002-T5, T-002-T6
- AC-IDs: AC-001, AC-002, AC-003, AC-005, AC-010
- Tests written:
  - T-002-T1: `UserTest` asserts the credentialed constructor and its role/hash/name/email invariants.
  - T-002-T1: `AccountBootstrapServiceTest.test_AC001_AC003_T002T1_bootstrapsAdminOnly` asserts ADMIN role, delegated hashing, result ID, and one successful transaction.
  - T-002-T1: authentication regression fixtures now use the credentialed `User` constructor; the defensive passwordless-row test uses the persistence constructor to model a legacy/hydrated invalid row.
  - T-002-T2: `AccountBootstrapServiceTest.test_AC001_AC002_AC003_T002T2_bootstrapsBothAccounts` asserts atomic ADMIN+USER creation, fixed roles, and distinct hashes.
  - T-002-T3: `AccountBootstrapServiceTest.test_AC005_T002T3_doesNotCreateAccountsWhenAdminExists` asserts safe refusal before hashing or account writes.
  - T-002-T4: tests cover invalid fields, duplicate emails within the request, and an email already persisted, with no partial creation.
  - T-002-T5: tests cover hashing and persistence failures, asserting rollback and failure audit outside the account transaction.
  - T-002-T6: Argon2id hash verification accepts the original password and rejects a different password.
- RED run: `mvnw.cmd "-Dtest=UserTest,AccountBootstrapServiceTest,AuthenticationServiceTest" test` — 17 tests, 15 assertion failures, 0 errors, 2 passed. Test sources compiled successfully. The 15 failures are assertions that the approved account constructor and bootstrap application port are not yet present; Argon2id verification and the passwordless-hydration login rejection passed. No production source was changed.
- Initial harness compile issue: record-style test probe accessors were corrected before the final RED run; it is not part of the recorded final result.
- Phase: RED — expected contract absence confirmed without compile errors.
- Timestamp: 2026-10-09T13:19:15Z

## T-002 — GREEN

- Test-IDs: T-002-T1, T-002-T2, T-002-T3, T-002-T4, T-002-T5, T-002-T6
- AC-IDs: AC-001, AC-002, AC-003, AC-005, AC-010
- Implementation:
  - Replaced the passwordless public `User` constructor with a credentialed constructor requiring an explicit role and rejecting blank identity/hash values.
  - Added the application bootstrap use case and its transaction, account repository, and audit ports; roles remain fixed by the use case.
  - Added field/email/duplicate validation, Argon2id delegation, safe failure codes, atomic success-audit orchestration, and failure-audit invocation after the transaction callback exits.
  - Redacted password in `AccountDetails.toString()`; safe exceptions contain only a reason code.
  - Updated authentication test fixtures to use the credentialed constructor; retained a persistence-hydration fixture for defensive login behavior.
  - Deferred CDI registration of the service until T-003 supplies its adapters; added the transaction adapter and CDI activation to T-003 scope after a full-suite Quarkus startup exposed the dependency-order constraint.
- Targeted run: `mvnw.cmd "-Dtest=UserTest,AccountBootstrapServiceTest,AuthenticationServiceTest" test` — 17 passed, 0 failed, 0 errors.
- Full backend run: `mvnw.cmd test` — 86 passed, 0 failed, 0 errors. Flyway v1–v9 and PostgreSQL migration tests passed.
- Phase: GREEN.
- Timestamp: 2026-10-09T13:24:13Z

## T-002 — REFACTOR

- Test-IDs: T-002-T1 through T-002-T6
- AC-IDs: AC-001, AC-002, AC-003, AC-005, AC-010
- Review: kept validation, account orchestration, duplicate checks, and safe failure auditing in separate focused methods. Deferred CDI registration until T-003 adapters exist so the application remains bootable between sequential tasks.
- Run result: `mvnw.cmd test` — 86 passed, 0 failed, 0 errors.
- Phase: REFACTOR — no behavior-changing refactor needed after review.
- Timestamp: 2026-10-09T13:24:55Z

## T-002 — SIMPLIFY

- Test-IDs: T-002-T1 through T-002-T6
- AC-IDs: AC-001, AC-002, AC-003, AC-005, AC-010
- Simplification: removed unused imports; no lint/format/static-analysis plugin is configured in the backend Maven build. `git diff --check` is the available whitespace validation.
- Validation: full backend suite remains green (86 passed); no new dependency or persistence behavior was introduced in T-002.
- Phase: SIMPLIFY — complete.
- Timestamp: 2026-10-09T13:24:55Z

## T-003 — RED

- Test-IDs: T-003-T1, T-003-T2, T-003-T3, T-003-T4, T-003-T5
- AC-IDs: AC-005, AC-011
- Tests written:
  - T-003-T1: `AccountBootstrapPersistenceTest.test_AC005_T003T1_transactionAdapterExposesTransactionalExecution` — verifies the PostgreSQL transaction adapter exposes the callback execution contract.
  - T-003-T2: `AccountBootstrapPersistenceTest.test_AC011_T003T2_successAuditAdapterCanRecordWithinAccountTransaction` — verifies the success-audit adapter contract.
  - T-003-T3: `AccountBootstrapPersistenceTest.test_AC011_T003T3_failureAuditAdapterCanPersistAfterRollback` — verifies the independent failure-audit operation contract.
  - T-003-T4: `AccountBootstrapPersistenceTest.test_AC011_T003T4_auditStorageFailureHasSafeLoggingFallback` — currently verifies the failure-audit handling seam exists; it does not yet assert emitted log contents.
  - T-003-T5: `AccountBootstrapPersistenceTest.test_AC011_T003T5_auditEntityContainsOnlyApprovedV9Columns` — asserts the entity has exactly the seven approved fields once present.
- Test support: added a UUID-scoped PostgreSQL schema, migrated by Quarkus through V1–V9; cleanup validates the schema name and drops only that isolated schema. PostgreSQL `currentSchema` retains `public` for the pgvector extension type. The profile generates temporary RSA keys for Quarkus startup and defines their cleanup.
- Harness correction: first run failed during V5 migration because `vector` was not in the isolated schema search path. The test URL was corrected to include `public`; the rerun initialized Quarkus and Flyway successfully.
- RED run: `mvnw.cmd "-Dtest=AccountBootstrapPersistenceTest" test` — 5 tests, 5 assertion failures, 0 errors. The failures identify the not-yet-created PostgreSQL transaction adapter, audit adapter, and audit entity. No production source was changed.
- Coverage note: these RED tests establish the adapter/entity contract boundary. T-003 GREEN must add behavioral assertions for advisory-lock serialization, atomic/surviving audit writes, safe logging contents, and actual persisted audit columns before the task can be considered complete.
- Phase: RED — expected missing-adapter assertions confirmed; behavioral integration coverage remains a GREEN-phase requirement.
- Timestamp: 2026-10-09T13:41:34Z

## Execution review — 2026-10-12

- T-002 revalidation: `mvnw.cmd "-Dtest=UserTest,AccountBootstrapServiceTest,AuthenticationServiceTest" test` — 17 passed, 0 failures, 0 errors. No production changes were needed for this rerun. PostgreSQL rollback remains a T-003 integration responsibility.
- T-003 correction: the earlier five structural failures do not establish behavioral RED. The reviewed execution plan requires transaction, concurrency, audit, and logging behavior tests before their production implementation. The earlier run is retained as historical evidence, not as permission to skip RED.
- Planning reconciliation: aligned Test-IDs and documented the composing account repository adapter; retained approved atomic bootstrap and durable audit decisions.
- Commit blocker: the documentation commit failed because SSH signing could not unlock the passphrase-protected signing key. `commit.gpgsign=true`; the Windows `ssh-agent` service is disabled and stopped. No signature setting was changed, no commit was created, and no push was attempted.
- Documentation changes remain staged separately from implementation. T-002 remains uncommitted; T-003 through T-005 remain incomplete. Resume task commits after the operator unlocks signing or explicitly authorizes an unsigned-commit exception.

## T-003 — RED (behavioral)

- Test-IDs: T-003-T1, T-003-T2, T-003-T3, T-003-T4, T-003-T5, T-003-T6
- AC-IDs: AC-001, AC-002, AC-005, AC-010, AC-011
- Tests written:
  - T-003-T1: real PostgreSQL concurrent bootstrap attempts and lock-release-after-rollback scenarios using bounded executor waits.
  - T-003-T2: success audit visibility after commit and injected success-audit failure with account rollback.
  - T-003-T3: account rollback followed by independent failure-audit persistence.
  - T-003-T4: injected audit repository failure with captured structured logs; sentinel password, hash, email, and exception text assertions.
  - T-003-T5: V9 audit-column allowlist and persisted audit data secret checks.
  - T-003-T6: duplicate database email conflict with safe failure and no partial account.
- Test support: every scenario uses the UUID-scoped PostgreSQL schema migrated through V1–V9 and clears only that schema's application rows before each test. The test profile retains public in currentSchema for pgvector resolution and removes temporary schemas/keys after the suite.
- RED run: mvnw.cmd -Dtest=AccountBootstrapPersistenceTest test — 8 tests, 8 assertion failures, 0 errors. Quarkus startup and Flyway migration succeeded. Failures identify the missing CDI service/adapters (PostgresAccountBootstrapTransaction, PostgresUserAccountRepository, PostgresAccountProvisioningAudit, and related resolvable ports); no production source was changed.
- Harness corrections before the recorded run: replaced an unavailable Quarkus InstanceHandle.isResolvable() call and converted checked reflection exceptions inside transaction suppliers; these were test-compilation issues and are not product failures.
- Phase: RED — behavioral PostgreSQL test contracts now fail for the expected missing-production-capability reason. T-003 may advance to GREEN.
- Timestamp: 2026-10-12T13:23:02-03:00
- Cleanup note: the profile did not remove two schemas after failing suites; they were explicitly identified and dropped manually after the run. T-003 GREEN must make cleanup reliable on failure and verify that no account-bootstrap test schema remains.

## T-003 — GREEN

- Test-IDs: T-003-T1, T-003-T2, T-003-T3, T-003-T4, T-003-T5, T-003-T6
- Implementation:
  - Added the PostgreSQL account repository adapter, including flush-before-ID and safe duplicate-email translation.
  - Added a transaction adapter using a PostgreSQL transaction-level advisory lock so bootstrap attempts are serialized and rollback releases the lock.
  - Added the V9 audit entity/repository and success/failure audit adapter; failure auditing runs in a new transaction and falls back to a reason-only structured log.
  - Registered the bootstrap service and adapters as unremovable CDI beans because the command entry point will resolve the use case dynamically.
  - Added a UUID-scoped integration profile with isolated Flyway migrations, temporary JWT keys, and schema cleanup.
- Targeted run: `mvnw.cmd "-Dtest=AccountBootstrapPersistenceTest" test` — 8 passed, 0 failed, 0 errors.
- Phase: GREEN — complete.
- Timestamp: 2026-10-12T13:53:26-03:00

## T-003 — REFACTOR / SIMPLIFY

- Review: kept transaction coordination, persistence adaptation, and audit persistence in separate adapters; no business rule was moved into infrastructure. The concurrency test now unwraps reflective invocation exceptions so it verifies the actual domain/application failure code.
- Safety review: audit rows contain only the seven approved V9 columns; fallback logging emits only outcome and reason code. No password, hash, email, or exception text is emitted by the adapter.
- Validation: targeted PostgreSQL suite remains green; `git diff --check` is required before commit. No new dependency or logging platform was introduced.
- Phase: REFACTOR / SIMPLIFY — complete.
- Validation: `mvnw.cmd test` — 94 passed, 0 failed, 0 errors; `mvnw.cmd clean verify` — build success. Explicitly removed the seven isolated schemas left by earlier failed RED executions; the final successful suite cleaned its own schema.
- Timestamp: 2026-10-12T13:58:00-03:00
