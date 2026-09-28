# Epic Technical Design — RAG and Knowledge Documents

## Scope

This epic delivers the first RAG capability for approved e-commerce knowledge
documents. The first delivery supports administrator-managed PDF and text-file
uploads, explicit knowledge scopes, document versioning, synchronous processing
on administrator request, and retrieval through PostgreSQL/pgvector.

Production-scale capabilities such as object storage, antivirus, asynchronous
workers, retries, external URLs, and user-owned documents remain future
evolution and are not required by this design.

## System Context

```text
Administrator
    |
    v
REST document management
    |
    v
Application document use cases
    |-----------------------> Document metadata/version repository
    |-----------------------> Original content storage abstraction
    |
    v
Synchronous processing workflow
    |
    +--> File validation and text extraction
    +--> Chunking
    +--> Embedding port
    +--> Chunk/vector repository (PostgreSQL + pgvector)

Authenticated user
    |
    v
Chat Resource -> Chat Application Service -> Scoped Retriever
                                      |
                                      +--> approved chunks
                                      +--> EcommerceAssistant
                                            |
                                            +--> ProductTool / OrderTool
```

## Architecture Boundaries

### Presentation

REST resources expose administrator document operations and existing chat
behavior. They validate transport concerns, map errors, and enforce the
administrator boundary. They do not parse files, generate embeddings, query
pgvector, or decide business policy.

### Application

Application services own document lifecycle use cases, processing orchestration,
scope-aware retrieval orchestration, and the rule that only active READY
content can enter the assistant context.

### Domain

The document model owns lifecycle state, version identity, publication state,
and valid transitions. It does not depend on REST, LangChain4j, OpenAI,
Hibernate, or pgvector.

### Infrastructure

Infrastructure adapts PostgreSQL/pgvector persistence, PDF/text extraction,
the configured embedding provider, and LangChain4j retrieval integration to
application ports. Provider-specific details remain outside the domain.

## Shared Decisions

- Document management requires `role=ADMIN`.
- Every document version has an explicit knowledge scope.
- Only the active version in `READY` state is eligible for retrieval.
- Upload creates `DRAFT`; processing is explicitly requested and synchronous.
- Processing failure results in `FAILED` and must not expose partial chunks.
- Unpublishing precedes physical removal.
- Source metadata is retained internally and is not added to the first REST
  response contract.
- Product, order, stock, payment, ownership, and authorization facts remain
  authoritative in application services and tools.

## Component Responsibilities

| Component | Responsibility |
|---|---|
| `KnowledgeDocumentResource` | ADMIN-only HTTP operations and request validation |
| `KnowledgeDocumentService` | Lifecycle, version, publication, and authorization-aware use cases |
| `DocumentProcessingService` | Transactional orchestration of extraction, chunking, embedding, and indexing |
| `DocumentTextExtractor` | Port for converting accepted files to text |
| `DocumentChunker` | Port/service for deterministic text segmentation |
| `EmbeddingService` | Port for converting chunks into vectors |
| `KnowledgeRetriever` | Scope- and state-aware retrieval for chat |
| Document repositories | Persist metadata, versions, states, and source content |
| Chunk/vector repository | Persist searchable chunks and vector representations |
| `EcommerceAssistant` | Generate the response using retrieved context and existing tools |

## Data Flows

### Upload

```text
ADMIN upload
  -> REST validation
  -> create document and version as DRAFT
  -> persist source metadata/content
  -> return document identifier and state
```

### Processing

```text
ADMIN process command
  -> verify document/version and valid DRAFT or FAILED transition
  -> mark PROCESSING
  -> extract text
  -> chunk text
  -> generate embeddings
  -> replace version's previous chunks atomically
  -> mark READY
```

Any processing failure marks the version `FAILED`. Partial chunks must not be
eligible for retrieval.

### Retrieval and chat

```text
authenticated chat request
  -> identify current user
  -> classify/use retrieval for knowledge questions
  -> filter active READY versions by explicit scope
  -> similarity search in pgvector
  -> pass retrieved context internally to assistant
  -> keep transactional questions on application Tools
```

## Data Model Overview

The persistence design is expected to contain:

- a logical knowledge document identity;
- version records with state, scope, source metadata, and content reference;
- chunk records linked to one version;
- vector representation for each searchable chunk;
- timestamps and processing failure information sufficient for operations.

New schema changes must be introduced through a new Flyway migration. Existing
migrations must not be modified.

## API Contract Sketch

The exact fields belong to the low-level design. The first contract is expected
to contain:

- `POST /api/knowledge-documents` — `ADMIN`, multipart upload, returns `201`
  with document/version identity and `DRAFT` state.
- `POST /api/knowledge-documents/{id}/process` — `ADMIN`, synchronous command,
  returns `200` with resulting state or deterministic processing failure.
- `POST /api/knowledge-documents/{id}/unpublish` — `ADMIN`, returns `200` with
  `UNPUBLISHED` state.
- `DELETE /api/knowledge-documents/{id}` — `ADMIN`, permitted only after
  unpublishing, returns `204`.

Errors use the existing API error shape with stable codes and no stack traces,
tokens, provider credentials, or raw sensitive document content.

## Security Posture

- All management operations require backend-enforced `ADMIN` authorization.
- Knowledge scope is evaluated before chunks are passed to the assistant.
- Client-provided scope, state, ownership, and publication values are not
  authoritative.
- Unpublished, failed, draft, and inactive versions are never retrievable.
- File upload input is untrusted and requires type, size, and content
  validation.
- Logs must identify operations without recording tokens or complete document
  contents.

## Observability Requirements

The first implementation must expose deterministic processing state and failure
information suitable for operational diagnosis. It should log document/version
identifiers and outcome, without raw content or secrets.

Metrics, alerts, tracing, retry dashboards, and queue monitoring are production
evolution items and are not required before the first delivery.

## Trade-offs and Alternatives

### Synchronous processing first

Synchronous processing keeps the first delivery simple and testable. It may
block a request for large files, but the application boundaries permit a later
worker-based implementation without changing the domain lifecycle.

### Database-backed source content first

Keeping the initial source content with application persistence reduces moving
parts. A content-storage port keeps a future object-storage migration isolated.

### Explicit scopes instead of user ownership

Explicit scopes support public and protected knowledge without introducing
per-user document ownership in this phase.

## Risks and Rollback

- A malformed or oversized file can consume resources; validate before parsing
  and enforce configured limits.
- Embedding-provider failure can leave processing incomplete; state transition
  to `FAILED` and exclude all incomplete content.
- Reprocessing can create duplicate vectors; replace chunks atomically per
  version.
- A bad document can produce incorrect answers; unpublish it and remove it
  from retrieval before physical cleanup.
- Schema rollback must use a forward Flyway migration or a controlled database
  rollback plan; applied migrations must not be rewritten.

## High-Level Test Scenario Map

### Lifecycle and API

- ADMIN uploads supported PDF and text files.
- Non-ADMIN upload/process/unpublish/delete is rejected.
- Unsupported type, empty file, and invalid content are rejected.
- Valid state transitions succeed; invalid transitions are rejected.
- Physical deletion requires `UNPUBLISHED`.

### Processing

- Successful extraction, chunking, embedding, and indexing produce READY.
- Extraction or embedding failure produces FAILED.
- Partial processing is not retrievable.
- Reprocessing does not duplicate active chunks.

### Retrieval and authorization

- READY active public content is retrieved for permitted users.
- Private scope is filtered for unauthorized users.
- DRAFT, FAILED, UNPUBLISHED, and inactive versions are excluded.
- No relevant result produces safe insufficient-knowledge behavior.

### AI boundaries

- Policy questions use retrieved context.
- Product and order questions continue to use authoritative application Tools.
- No live OpenAI key is required for unit tests.

## Open Questions

None. Technical defaults not material to the product intent will be specified
in the low-level design and remain replaceable through ports.

## Resolved Questions

The design carries forward all decisions from `01-spec.md` and introduces no
new unresolved product decisions.
