# Tasks — RAG and Knowledge Documents

## T-001: Define document domain lifecycle

- AC-IDs: AC-001, AC-007
- Test-IDs: T-001-T1 (domain unit), T-001-T2 (invalid transitions)
- Files in scope: `domain/knowledge/KnowledgeDocument.java`, `KnowledgeDocumentVersion.java`, `KnowledgeDocumentState.java`, matching tests
- Dependencies: none
- Gates: unit
- Rollback: revert commit; no schema change
- Notes: Enforce version identity, state transitions, and active-version rules.

## T-002: Define document persistence schema and repositories

- AC-IDs: AC-001, AC-002, AC-005, AC-007
- Test-IDs: T-002-T1 (repository unit), T-002-T2 (migration/persistence)
- Files in scope: Flyway migration, persistence entities/repositories, matching tests
- Dependencies: T-001
- Gates: unit, persistence
- Rollback: revert commit before migration is applied; otherwise forward migration required
- Notes: Include document versions, chunks, scope, state, source content, and vector storage.

## T-003: Implement ADMIN upload and lifecycle API

- AC-IDs: AC-001, AC-007, AC-008
- Test-IDs: T-003-T1 (application), T-003-T2 (REST contract), T-003-T3 (authorization)
- Files in scope: document resource, requests/responses, application service, tests, error mappers
- Dependencies: T-001, T-002
- Gates: unit, REST, security
- Rollback: revert commit; migration remains compatible if schema is unused
- Notes: Support PDF/text upload, explicit scope, DRAFT creation, unpublish, and guarded deletion.

## T-004: Implement extraction, chunking, and embedding ports

- AC-IDs: AC-002, AC-007
- Test-IDs: T-004-T1 (text extractor), T-004-T2 (PDF extractor), T-004-T3 (chunker/embedder contracts)
- Files in scope: application ports/services, infrastructure adapters, tests, `pom.xml`
- Dependencies: T-001
- Gates: unit, dependency/build
- Rollback: revert commit and dependency; no API migration required
- Notes: Keep provider calls replaceable and deterministic in tests.

## T-005: Implement synchronous processing workflow

- AC-IDs: AC-002, AC-007
- Test-IDs: T-005-T1 (successful processing), T-005-T2 (failure state), T-005-T3 (atomic replacement)
- Files in scope: processing application service, repositories, tests
- Dependencies: T-002, T-004
- Gates: unit, persistence
- Rollback: revert commit; failed versions remain unavailable
- Notes: PROCESSING to READY/FAILED with no partial retrievable chunks.

## T-006: Implement scoped retrieval

- AC-IDs: AC-003, AC-005, AC-008
- Test-IDs: T-006-T1 (state/version filtering), T-006-T2 (scope filtering), T-006-T3 (no result)
- Files in scope: retriever port/service, vector repository, tests
- Dependencies: T-002, T-005
- Gates: unit, persistence, security
- Rollback: revert commit; existing chat remains without document context
- Notes: Preserve source identity internally.

## T-007: Integrate retrieval with the assistant

- AC-IDs: AC-003, AC-004, AC-006
- Test-IDs: T-007-T1 (policy context), T-007-T2 (no-context response), T-007-T3 (tool authority)
- Files in scope: chat application flow, AI service integration, prompt/context adapter, tests
- Dependencies: T-006
- Gates: unit, AI boundary
- Rollback: revert commit; tools continue to serve transactional requests
- Notes: Never replace ProductTool or OrderTool with document content.

## T-008: Complete validation and production evolution record

- AC-IDs: AC-001, AC-002, AC-003, AC-004, AC-005, AC-006, AC-007, AC-008
- Test-IDs: T-008-T1 (full suite), T-008-T2 (clean verify), T-008-T3 (traceability/security review)
- Files in scope: validation report, traceability, code review, production evolution notes
- Dependencies: T-001 through T-007
- Gates: full validation, security review, documentation
- Rollback: revert documentation only; no production code change
- Notes: Close the epic only when all gates pass.

## Parallelization

T-001 and T-004 can be designed independently, but implementation is kept
sequential because T-004 consumes the domain contract. T-006 depends on indexed
data from T-005. T-007 and T-008 remain sequential gates.
