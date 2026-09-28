# Validation Report — RAG and Knowledge Documents

## Harness Results

| Check | Result | Evidence |
|---|---|---|
| Unit test suite | PASS | `mvnw.cmd -q -DskipITs=true test` |
| Clean verification | PASS | `mvnw.cmd -q clean verify -DskipITs=true` |
| Quarkus build/CDI validation | PASS | `clean verify` completed after explicit `@Inject` fix |
| Migration review | PASS | V4 and V5 are versioned; prior migrations unchanged |
| Secret review | PASS | No credentials, tokens, or keys added |

The repository has no configured lint, coverage, or live integration-test
harness beyond the Maven/Quarkus validation above. The existing Mockito
dynamic-agent warning is informational and does not fail the build.

## Compliance Matrix

| Requirement | Status | Evidence |
|---|---|---|
| AC-001 | Met | Knowledge document/version domain model, V4 migration, lifecycle tests |
| AC-002 | Met | Processing service, chunk persistence, V5 migration, processing tests |
| AC-003 | Met | Scoped retriever and chat context propagation test |
| AC-004 | Met | Safe no-context instruction in `ChatApplicationService` |
| AC-005 | Met | `RetrievedKnowledge` preserves document/version/chunk identity |
| AC-006 | Met | Existing ProductTool/OrderTool flow remains separate; chat integration test covers context boundary |
| AC-007 | Met | FAILED transition, atomic chunk replacement, processing failure tests |
| AC-008 | Met | pgvector query filters scope before returning chunks |

## Production Evolution Boundary

The phase is complete for the approved first delivery. The following remain
future production work: object storage, antivirus scanning, asynchronous
workers, retries, queue monitoring, configurable limits, richer operational
metrics, and user-owned documents.

## Risks

| Risk | Severity | Mitigation |
|---|---|---|
| Synchronous processing can hold a request for a large file | medium | Move the same application use case behind a worker in the production evolution |
| Source files are initially stored in the database | medium | Storage is isolated conceptually and can be replaced by an object-storage adapter |
| Live embedding calls require OpenAI configuration | medium | Unit tests use ports/fakes; runtime configuration remains environment-based |
| Full DB integration was not executed in this local run | low | Flyway and Quarkus build validation passed; execute Testcontainers/PostgreSQL validation before deployment |

## Decision

**Go for the approved Phase 10 first delivery.**

This is not a claim that production hardening is complete. It means the
approved simple scope is implemented, tested, documented, and separated from
the explicitly deferred production evolution.
