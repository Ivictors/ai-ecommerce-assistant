# Epic Roadmap — Authentication and Authorization

## Slices

### S-001 — Authenticated identity

- Scope: resolve `SecurityIdentity`, validate numeric `sub`, and represent anonymous/authenticated users.
- AC focus: AC-001, AC-002, AC-003.
- Dependencies: existing JWT configuration.
- Intent: establish one trusted source for the current user.

### S-002 — Endpoint and administrator matrix

- Scope: apply the public, customer, and `role=ADMIN` matrix to current endpoints.
- AC focus: AC-009, AC-010, AC-011, AC-014.
- Dependencies: S-001; error contract from issue #5.
- Intent: protect the REST boundary without trusting the frontend.

### S-003 — Order and conversation ownership

- Scope: stop trusting client-provided `userId` and allow access only to owned resources.
- AC focus: AC-004, AC-005, AC-006, AC-007, AC-008.
- Dependencies: S-001; S-002.
- Intent: close the risk of cross-customer access.

### S-004 — Tool and AI flow authorization

- Scope: ensure Tools use application services and preserve identity/ownership.
- AC focus: AC-012, AC-013, AC-014.
- Dependencies: S-001; S-003.
- Intent: prevent security bypass through the LLM.

### S-005 — Integrated validation and security observability

- Scope: REST/integration tests, error responses, safe logs, and end-to-end validation.
- AC focus: AC-015 and all previous ACs.
- Dependencies: S-001 through S-004; issue #5.
- Intent: demonstrate that security works at the real application boundary.

## Dependency graph

```text
S-001
  |
  +--> S-002 ----+
  |              |
  +--> S-003 ----+--> S-005
         |
         +--> S-004 -+
```

## Milestone mapping

- Milestone M1: S-001 and S-002 — identity and endpoints.
- Milestone M2: S-003 — private-data ownership.
- Milestone M3: S-004 — secure AI.
- Milestone M4: S-005 — validation and completion gate.

## Exit criteria

- All ACs have traceable tasks and tests.
- Unit and REST/security tests pass.
- No endpoint in the matrix remains without corresponding protection.
- No Tool accesses a repository or database directly.
- The error contract is approved and used.
