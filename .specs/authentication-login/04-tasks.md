# Tasks — Backend Login and JWT Issuance

### T-001: Add persisted credential and role model

- AC-IDs: AC-002, AC-003, AC-005
- Test-IDs: T-001-T1 (entity), T-001-T2 (migration/seed)
- Files: `User`, `UserRepository`, Flyway migration, tests
- Dependencies: none
- Gates: unit, persistence

### T-002: Implement Argon2id password port

- AC-IDs: AC-005, AC-009
- Test-IDs: T-002-T1 (hash), T-002-T2 (match/mismatch)
- Files: application password port, infrastructure adapter, tests, `pom.xml`
- Dependencies: T-001
- Gates: unit, dependency review

### T-003: Implement RSA token issuer

- AC-IDs: AC-002, AC-003, AC-006, AC-007, AC-008
- Test-IDs: T-003-T1 (claims), T-003-T2 (expiration), T-003-T3 (signature)
- Files: token port/adapter, runtime configuration, tests
- Dependencies: T-001
- Gates: unit, security

### T-004: Implement authentication application service

- AC-IDs: AC-001, AC-004, AC-005
- Test-IDs: T-004-T1 (valid credentials), T-004-T2 (invalid credentials)
- Files: authentication service, exceptions, tests
- Dependencies: T-001, T-002, T-003
- Gates: unit, security

### T-005: Implement login REST contract

- AC-IDs: AC-001, AC-004, AC-006
- Test-IDs: T-005-T1 (200 response), T-005-T2 (401/400 responses)
- Files: resource, request/response DTOs, mappers, tests
- Dependencies: T-004
- Gates: REST, contract

### T-006: Validate end-to-end authentication

- AC-IDs: AC-006, AC-007, AC-008, AC-009, AC-010
- Test-IDs: T-006-T1 (protected endpoint), T-006-T2 (role matrix), T-006-T3 (secret review)
- Files: integration tests, validation report, traceability, code review
- Dependencies: T-005
- Gates: full verification, security review

## Deferred Work

- Issue #42: refresh-token lifecycle.
- Issue #43: password recovery.
- Issue #44: secure administrative credential provisioning for users whose
  password hash is null.
