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
