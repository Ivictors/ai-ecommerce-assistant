# Low-Level Tasks — Angular Authentication Session

## API and security details

- Login request: `{ email: string; password: string }`.
- Login response: `{ accessToken: string; tokenType: string; expiresIn: number }`.
- Only the access token is persisted, under the existing application-specific storage key.
- The interceptor sends the token only to `/api/` requests on the configured backend boundary.
- `401` and malformed authentication responses fail closed; detailed backend internals are not displayed.

### T-001: Implement login contract and authentication service

- AC-IDs: AC-001, AC-002, AC-006, AC-008
- Test-IDs: T-001-T1 (valid login), T-001-T2 (invalid login), T-001-T3 (malformed response), T-001-T4 (no password persistence)
- Files in scope: `frontend/ai-ecommerce-assistant/src/app/core/auth/auth.model.ts`, `auth.service.ts`, corresponding specs
- Dependencies: none
- Gates: unit, HTTP contract
- Rollback: revert focused frontend commit; no schema change

### T-002: Implement login screen and navigation

- AC-001, AC-002, AC-007
- Test-IDs: T-002-T1 (form validation), T-002-T2 (success navigation), T-002-T3 (safe failure), T-002-T4 (loading state)
- Files in scope: `src/app/features/auth/login/*`, routes, corresponding specs
- Dependencies: T-001
- Gates: component, routing
- Rollback: revert focused frontend commit; no schema change

### T-003: Restrict bearer interceptor and complete session lifecycle

- AC-003, AC-004, AC-005, AC-007, AC-008
- Test-IDs: T-003-T1 (API bearer header), T-003-T2 (unrelated-origin negative), T-003-T3 (missing token), T-003-T4 (logout/guard)
- Files in scope: `auth.interceptor.ts`, `auth.guard.ts`, `auth.service.ts`, app configuration, corresponding specs
- Dependencies: T-001
- Gates: interceptor, routing, security
- Rollback: revert focused frontend commit; no schema change

### T-004: Full frontend verification

- AC-001 through AC-008
- Test-IDs: T-004-T1 (frontend suite), T-004-T2 (production build), T-004-T3 (secret review)
- Files in scope: feature validation artifacts and only required corrections
- Dependencies: T-001, T-002, T-003
- Gates: full frontend validation, security review
- Rollback: revert validation correction commit if required

## TDD State

Each task must follow `pending → red → green → refactor → done`. A task is not complete until its tests pass and the implementation log records every phase.
