# Implementation Log

## T-001 — RED

- Test-IDs: S001-T1, S001-T2, S001-T3, S001-T4, S001-T6, S001-T7
- AC-IDs: AC-001, AC-002, AC-003
- Tests written:
  - S001-T1: `CurrentUserServiceTest.rejectsAnonymousUser`
  - S001-T2: `CurrentUserServiceTest.rejectsAuthenticatedIdentityWithoutPrincipal`
  - S001-T3: `CurrentUserServiceTest.rejectsNonNumericPrincipal`
  - S001-T4: `CurrentUserServiceTest.rejectsEmptyPrincipal`
  - Existing valid numeric and anonymous scenarios retained.
- Expected RED result: newly added malformed-principal assertions fail because the current implementation exposes generic runtime failures rather than the approved security failure behavior.
- Phase: RED
- Timestamp: 2026-09-23

## T-001/T-002 — GREEN

- Test-IDs: S001-T1 through S001-T7
- AC-IDs: AC-001, AC-002, AC-003
- Implementation:
  - Added `UnauthenticatedUserException`.
  - Validated anonymous, missing, blank, and non-numeric principals.
  - Converted valid numeric principals to `Long`.
- Run result: all slice tests passed.
- Phase: GREEN
- Timestamp: 2026-09-23
