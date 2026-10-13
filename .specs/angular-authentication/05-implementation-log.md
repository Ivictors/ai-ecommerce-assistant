# Implementation Log — Angular Authentication Session

No implementation phase has started yet. RED tests will be recorded task by task before production code changes.

## T-001 — RED

- Test-IDs: T-001-T1, T-001-T2, T-001-T3, T-001-T4
- AC-IDs: AC-001, AC-002, AC-006, AC-008
- Tests written: `auth.service.spec.ts` covers successful login, authentication failure, malformed response, and password non-persistence.
- Expected result: tests fail because `AuthService.login` is not implemented.
- Phase: RED
- Timestamp: 2026-10-13T00:05:00-03:00

## T-001 — GREEN

- Test-IDs: T-001-T1, T-001-T2, T-001-T3, T-001-T4
- AC-IDs: AC-001, AC-002, AC-006, AC-008
- Implementation: added the typed login contract and `AuthService.login` with fail-closed response validation and session storage.
- Run result: pending targeted frontend test run.
- Phase: GREEN
- Timestamp: 2026-10-13T00:20:00-03:00

## T-001 — REFACTOR / SIMPLIFY

- Kept the implementation limited to the approved login contract; no speculative token renewal or role parsing was added.
- Passwords remain request-only and are not written to browser storage or logs.
- Phase: REFACTOR / SIMPLIFY
- Timestamp: 2026-10-13T00:20:00-03:00

## T-002 — RED

- Test-IDs: T-002-T1, T-002-T2, T-002-T3
- AC-IDs: AC-001, AC-007
- Tests written: `app.routes.spec.ts` requires a resolvable standalone login route and public entry redirect.
- Expected result: tests fail because the login route and component do not exist yet.
- Phase: RED
- Timestamp: 2026-10-13T00:30:00-03:00

## T-002 — GREEN

- Test-IDs: T-002-T1, T-002-T2, T-002-T3, T-002-T4
- AC-IDs: AC-001, AC-002, AC-007
- Implementation: added the standalone login component, reactive form validation, safe failure message, loading state, and `/login` route; tests exercise real HTTP success and `401` behavior.
- Run result: 12 tests passed in 4 files.
- Phase: GREEN
- Timestamp: 2026-10-13T00:55:00-03:00

## T-002 — REFACTOR / SIMPLIFY

- Kept navigation limited to the approved `/private` destination.
- Kept authorization out of the component; the backend remains authoritative.
- Phase: REFACTOR / SIMPLIFY
- Timestamp: 2026-10-13T00:55:00-03:00

## T-003 — RED

- Test-IDs: T-003-T1, T-003-T2, T-003-T3, T-003-T4
- AC-IDs: AC-003, AC-004, AC-005, AC-007
- Tests written: `auth.interceptor.spec.ts` covers backend, unrelated-origin, and no-session requests; `auth.guard.spec.ts` covers redirect and logout.
- Expected result: the unrelated-origin test fails because the current interceptor adds the token to every request.
- Phase: RED
- Timestamp: 2026-10-13T01:10:00-03:00

## T-003 — GREEN

- Test-IDs: T-003-T1, T-003-T2, T-003-T3, T-003-T4
- AC-IDs: AC-003, AC-004, AC-005, AC-007
- Implementation: restricted bearer propagation to same-origin `/api/` requests and preserved token-free behavior for other requests.
- Run result: 18 tests passed in 6 files.
- Phase: GREEN
- Timestamp: 2026-10-13T01:30:00-03:00

## T-003 — REFACTOR / SIMPLIFY

- The URL predicate is isolated, explicit, and fails closed for malformed URLs.
- No client-side role or ownership authorization was introduced.
- Phase: REFACTOR / SIMPLIFY
- Timestamp: 2026-10-13T01:30:00-03:00

## T-004 — RED

- Test-IDs: T-004-T1, T-004-T2
- AC-IDs: AC-003
- Tests written: `private.component.spec.ts` requires a visible logout action that clears the session and navigates to `/login`.
- Expected result: tests fail because the private shell has no logout action.
- Phase: RED
- Timestamp: 2026-10-13T01:50:00-03:00

## T-004 — GREEN

- Test-IDs: T-004-T1, T-004-T2
- AC-IDs: AC-003
- Implementation: added a visible private-shell logout action that clears the session and navigates to `/login`.
- Run result: 20 tests passed in 7 files; production build passed.
- Phase: GREEN
- Timestamp: 2026-10-13T02:20:00-03:00

## T-004 — REFACTOR / SIMPLIFY

- Kept logout behavior in the private shell and reused `AuthService.clearSession()`.
- Removed the stale placeholder text claiming login was not implemented.
- No refresh-token or client-side authorization behavior was introduced.
- Phase: REFACTOR / SIMPLIFY
- Timestamp: 2026-10-13T02:20:00-03:00
