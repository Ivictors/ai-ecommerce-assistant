# Epic Roadmap — RAG and Knowledge Documents

## Slices

### S-001 — RAG architecture and contracts

- Issue: #19
- Scope: high-level design, shared boundaries, ADRs, and roadmap.
- Acceptance focus: AC-001 through AC-008 traceability.
- Dependencies: #18 completed.

### S-002 — Low-level design and test decomposition

- Issue: #20
- Scope: lifecycle model, API details, persistence shape, task IDs, and test IDs.
- Dependencies: S-001.

### S-003 — Knowledge document lifecycle

- Issue: #21
- Scope: ADMIN upload, validation, versioning, state transitions, unpublish,
  and deletion rules.
- Acceptance focus: AC-001, AC-007, AC-008.
- Dependencies: S-002.

### S-004 — Synchronous processing and vector indexing

- Issue: #22
- Scope: PDF/text extraction, deterministic chunking, embedding port, and
  pgvector persistence.
- Acceptance focus: AC-002, AC-007.
- Dependencies: S-003.

### S-005 — Scoped knowledge retrieval

- Issue: #23
- Scope: active READY version filtering, explicit scope authorization, and
  internal source metadata.
- Acceptance focus: AC-003, AC-005, AC-008.
- Dependencies: S-004.

### S-006 — AI assistant integration

- Issue: #24
- Scope: provide retrieved context to knowledge questions while preserving
  ProductTool and OrderTool as authoritative.
- Acceptance focus: AC-003, AC-004, AC-006.
- Dependencies: S-005.

### S-007 — Verification and production evolution plan

- Issue: #25
- Scope: full validation, traceability, security review, and future production
  hardening documentation.
- Acceptance focus: all ACs.
- Dependencies: S-006.

## Dependency Graph

```text
S-001 (#19)
    |
    v
S-002 (#20)
    |
    v
S-003 (#21) --> S-004 (#22) --> S-005 (#23) --> S-006 (#24) --> S-007 (#25)
```

## Milestones

- M1: Architecture and low-level contracts — S-001, S-002.
- M2: Governed document lifecycle — S-003.
- M3: Searchable indexed knowledge — S-004, S-005.
- M4: Grounded assistant responses and validation — S-006, S-007.

## Exit Criteria

- All ACs map to tasks, tests, and implementation.
- Only approved document states and scopes reach retrieval.
- Transactional tools remain authoritative.
- Full validation passes with no blocker or major finding.
- Production evolution items are documented separately from the simple first
  delivery.
