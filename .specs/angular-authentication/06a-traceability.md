# Traceability — Angular Authentication Session

| AC | Task | Tests | Code | Gate |
|---|---|---|---|---|
| AC-001 | T-001/T-002 | `auth.service.spec.ts`, `login.component.spec.ts` | `AuthService.login`, `LoginComponent.submit` | PASS |
| AC-002 | T-001/T-002 | authentication failure and safe error tests | `catchError`, `errorMessage` | PASS |
| AC-003 | T-003/T-004 | clear-session and logout tests | `AuthService.clearSession`, `PrivateComponent.logout` | PASS |
| AC-004 | T-003 | backend API interceptor test | `authInterceptor` | PASS |
| AC-005 | T-003 | unrelated-origin/no-token tests | `isBackendApiRequest` | PASS |
| AC-006 | T-001 | malformed response test | `AuthService.login` response validation | PASS |
| AC-007 | T-002/T-003 | route and guard tests | `routes`, `authGuard` | PASS |
| AC-008 | T-001/T-003 | storage and source review | storage/interceptor implementation | PASS |
