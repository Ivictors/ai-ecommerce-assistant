# Epic Roadmap — AI E-Commerce Assistant MVP

## Ordered Slices

### S-001 — Backend login and JWT issuance

- Issue: #35 — complete (`1b34e54`, published on `main`).
- Scope: Argon2id credentials, RSA access tokens, login contract, server-side
  verification and role enforcement.
- Acceptance focus: AC-001, AC-011.
- Dependencies: approved security decisions; complete.

### S-002 — Authenticated conversation creation

- Issue: #47 — open.
- Scope: create a conversation owned only by the verified caller and return a
  usable identifier.
- Acceptance focus: AC-004, AC-010, AC-011.
- Dependencies: #35; complete before #39/#40.

### S-003 — Secure account credential provisioning

- Issue: #44 — open; design/security decision gate.
- Scope: safely establish initial ADMIN and provision/reset credentials for
  existing accounts with null hashes.
- Acceptance focus: AC-001, AC-011, AC-012.
- Dependencies: #35.
- Gate: select trusted first-ADMIN bootstrap authority before implementation.

### S-004 — Angular authentication and session

- Issue: #36 — open.
- Scope: login request/response, access token in sessionStorage, logout, and
  interceptor integration.
- Acceptance focus: AC-001, AC-011.
- Dependencies: #35 and #44.

### S-005 — Angular REST error behavior

- Issue: #37 — open.
- Scope: map 401/403/404/409/422/500 and clear stale local session on 401.
- Acceptance focus: safe failures in AC-001, AC-003–AC-005, AC-010–AC-012.
- Dependencies: #36.

### S-006 — Customer product catalog

- Issue: #38 — open.
- Scope: product list and detail using backend-owned product values.
- Acceptance focus: AC-002.
- Dependencies: #37.

### S-007 — Orders and conversations UI

- Issue: #39 — open.
- Scope: own-order and conversation views, start/select conversation using a
  non-null backend-issued identifier.
- Acceptance focus: AC-003, AC-004, AC-010.
- Dependencies: #36, #37, #47.

### S-008 — Structured assistant chat UI

- Issue: #40 — open.
- Scope: render typed chat response variants, schema version, loading, retry,
  and no-context behavior.
- Acceptance focus: AC-005, AC-007–AC-009.
- Dependencies: #39 and the approved structured-output contract.

### S-009 — MVP end-to-end validation

- Issue: #41, then close umbrella #12 — open.
- Scope: frontend build/tests, full customer journey, API contract/security
  checks, accessibility baseline, environment docs, controlled backend/data.
- Acceptance focus: AC-001–AC-012.
- Dependencies: #36–#40, #44, #47.

### S-010 — Observability for implemented MVP components

- Issue: #13 — open, acceptance criteria narrowed to current API, database,
  LLM, and document ingestion flows.
- Scope: request correlation, safe logs, current-component metrics and
  operational notes.
- Dependencies: core MVP flows; expand only when future provider integrations
  are approved and implemented.

### S-011 — Reproducible containers and release/rollback plan

- Issue: #14 — open.
- Scope: document/configure local backend, frontend, and database execution;
  health checks, migration handling, secrets, rollback classification and a
  deploy plan. Do not deploy as part of this slice.
- Dependencies: MVP runtime shape and #13 operational signals.

### S-012 — Final hardening and technical handoff

- Issue: #15 — open.
- Scope: full clean verification, security review, traceability, documentation,
  limitations and go/no-go classification.
- Dependencies: completed MVP, #13 and #14.

## MVP Milestones

- **M1 — Backend prerequisites:** S-001, S-002, S-003.
- **M2 — Customer experience:** S-004 through S-008.
- **M3 — Validated usable MVP:** S-009; close #12 only here.
- **M4 — Operable and reviewed delivery:** S-010 through S-012.

## Acceptance Traceability

| Acceptance criteria | Existing/planned issues |
| --- | --- |
| AC-001 — customer authentication | #35 complete; #44, #36, #41 planned |
| AC-002 — catalog | #38, #41 planned |
| AC-003 — own orders | #39, #41 planned |
| AC-004 — create owned conversation | #47, #39, #41 planned |
| AC-005 — structured chat response | #40, #41 planned |
| AC-006 — approved document ingestion/publication | RAG slices #21–#25 complete; #41 end-to-end validation planned |
| AC-007–AC-009 — grounded policy and authoritative product/order response | RAG #24 complete; #40, #41 planned |
| AC-010 — ownership isolation | #47, #39, #41 planned; existing backend ownership checks |
| AC-011 — server-side authentication | #35 complete; #36, #37, #41 planned |
| AC-012 — administrator operations | Existing backend authorization/tests; #41 integrated validation planned |

## Dependency Graph

```text
#35
  +--> #47 -----------------------> #39 --> #40
  +--> #44 --> #36 --> #37 --> #38   |       \
                   +---------------> #39      \
                  #38/#39/#40/#44/#47 --> #41 --> #12 close

MVP validated (#41) --> #13 --> #14 --> #15
```

## Deferred Beyond MVP

- #42 refresh-token lifecycle and #43 password recovery: revisit before a
  public production release; they are not hidden prerequisites for local MVP
  acceptance.
- Transactional commerce: no implementation issues exist yet for product
  variants, stock/reservations, cart/checkout, payments, promotions, returns,
  delivery, or notifications. These remain explicit non-goals until separately
  specified and approved.
- No public deployment is authorized by this roadmap; #14 produces a plan,
  while #15 provides the final review gate.
