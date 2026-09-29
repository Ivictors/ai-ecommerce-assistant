# Validation Report — Selective Structured Output

## Harness Results

| Check | Result | Evidence |
|---|---|---|
| Unit tests | PASS | `mvnw.cmd -q -DskipITs=true test` |
| Clean verification | PASS | `mvnw.cmd -q clean verify -DskipITs=true` |
| Quarkus/CDI build | PASS | Included in `clean verify` |
| Contract DTO compilation | PASS | Typed response tests and Maven build |
| Secret review | PASS | No secrets or provider credentials added |

The repository does not currently provide a dedicated frontend or live REST
integration harness. The existing Mockito dynamic-agent warning is
informational and does not fail the build.

## Compliance Matrix

| AC | Status | Evidence |
|---|---|---|
| AC-001 | Met | `ChatResponse` JSON envelope |
| AC-002 | Met | `ChatResponse.text` and general chat route |
| AC-003 | Met | `ProductInformationData` mapped from `ProductService` |
| AC-004 | Met | `OrderStatusData` mapped after `findByIdForUser` |
| AC-005 | Met | `PolicyInformationData` mapped from `KnowledgeRetriever` |
| AC-006 | Met | Policy no-context returns `FALLBACK` |
| AC-007 | Met | `IntentValidator` maps invalid/incomplete values to `UNKNOWN` |
| AC-008 | Met | Model values are not copied into transactional payloads |
| AC-009 | Met | `schemaVersion = "1"` in every response factory |
| AC-010 | Met | Existing conversation ownership and role boundaries remain active |

## Angular Contract Documentation

The future Angular client should switch on `type`, use `schemaVersion` for
compatibility checks, display `message`, and only read `data` for the matching
type. It must not parse `message` to derive product, order, or authorization
data.

## Risks

- Changing the current response from plain text to JSON requires the future
  Angular client to consume the new envelope.
- Live REST and PostgreSQL integration should be executed before deployment.
- The LLM still produces the initial classification; the backend validator and
  authoritative services remain mandatory safeguards.

## Decision

**GO** for Phase 13 first delivery. The selective structured-output contract is
implemented and validated. Frontend implementation remains issue #12.
