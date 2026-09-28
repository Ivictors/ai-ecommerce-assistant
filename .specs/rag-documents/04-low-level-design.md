# Low-Level Design — RAG and Knowledge Documents

## API Contracts

All endpoints are under `/api/knowledge-documents` and require
`@RolesAllowed("ADMIN")`. Responses use the existing `ApiErrorResponse` shape
for failures.

### Create document

`POST /api/knowledge-documents`

Content type: `multipart/form-data`

Required parts:

- `file`: PDF or text file;
- `scope`: explicit knowledge scope.

Successful response: `201 Created`

```json
{
  "id": 1,
  "version": 1,
  "filename": "return-policy.pdf",
  "mediaType": "application/pdf",
  "scope": "PUBLIC",
  "state": "DRAFT"
}
```

### Process document

`POST /api/knowledge-documents/{id}/process`

No request body. Processing is synchronous in the first delivery.

Successful response: `200 OK`

```json
{
  "id": 1,
  "version": 1,
  "state": "READY"
}
```

Processing failure response: `422 Unprocessable Entity`

```json
{
  "code": "DOCUMENT_PROCESSING_FAILED",
  "message": "The document could not be processed"
}
```

### Unpublish document

`POST /api/knowledge-documents/{id}/unpublish`

Successful response: `200 OK` with state `UNPUBLISHED`.

### Delete document

`DELETE /api/knowledge-documents/{id}`

The document must already be `UNPUBLISHED`.

Successful response: `204 No Content`.

### Common errors

| Condition | Status | Code |
|---|---:|---|
| Unauthenticated | 401 | `UNAUTHENTICATED` |
| Authenticated without ADMIN | 403 | `FORBIDDEN` |
| Invalid multipart input | 400 | `INVALID_REQUEST` |
| Unsupported file or scope | 400 | `INVALID_DOCUMENT` |
| Document not found | 404 | `RESOURCE_NOT_FOUND` |
| Invalid lifecycle transition | 409 | `INVALID_DOCUMENT_STATE` |
| Delete before unpublish | 409 | `DOCUMENT_MUST_BE_UNPUBLISHED` |
| Processing failure | 422 | `DOCUMENT_PROCESSING_FAILED` |
| Unexpected failure | 500 | `INTERNAL_ERROR` |

## Data Models

### Domain concepts

```text
KnowledgeDocument
  id
  currentVersionId

KnowledgeDocumentVersion
  id
  documentId
  versionNumber
  filename
  mediaType
  scope
  state
  sourceContent
  failureReason
  createdAt
  processedAt
```

States:

```text
DRAFT -> PROCESSING -> READY
                  \-> FAILED -> PROCESSING
READY -> UNPUBLISHED
FAILED -> UNPUBLISHED
DRAFT -> UNPUBLISHED
UNPUBLISHED -> physical deletion
```

Only the current version in `READY` state is retrievable. A new upload for an
existing logical document creates a new version and makes the prior version
inactive. The exact replacement endpoint is deferred to the implementation
contract; the first lifecycle slice may create a new logical document while
retaining the version model.

### Searchable chunk

```text
KnowledgeChunk
  id
  documentVersionId
  chunkIndex
  content
  embedding
  sourceMetadata
```

`embedding` is stored in a pgvector column. `sourceMetadata` contains internal
document/version/chunk identity and is not exposed by the first chat response.

### Migration strategy

Add a new Flyway migration after the existing migrations. It must:

- create document, version, and chunk tables;
- create the vector column using the dimension required by the selected
  embedding adapter;
- add state/scope checks;
- add foreign keys and indexes for active-version and scope filtering;
- avoid modifying V1, V2, or V3.

## Error Model

Errors are mapped at the REST boundary and must not expose parser messages,
provider credentials, stack traces, or source document contents.

Application exceptions:

- `InvalidDocumentException` — unsupported media type, empty content, or
  malformed input;
- `InvalidDocumentStateException` — command is not valid for the current state;
- `DocumentNotFoundException` — no accessible document exists;
- `DocumentProcessingException` — extraction, chunking, embedding, or indexing
  failed;
- `KnowledgeScopeAccessException` — retrieval scope is not permitted.

The processing use case must ensure a failure leaves no active searchable
chunks and records deterministic `FAILED` state.

## Security Details

- Every management endpoint requires `ADMIN` at the backend boundary.
- The client cannot choose `READY`, `PROCESSING`, or publication state.
- Filename, media type, size, and content are untrusted input.
- Accepted media types are PDF and plain text/Markdown variants selected by the
  validator; unknown types are rejected.
- Empty files and parser results with no usable text are rejected.
- Scope is validated against a closed enum and filtered during retrieval.
- Retrieval never returns inactive, failed, draft, or unpublished versions.
- Logs include identifiers and outcomes only, never raw files, tokens, or
  complete document content.

## Test Strategy

- Domain unit tests for lifecycle transitions and version activation.
- Application unit tests with fake extractor, chunker, embedder, and repositories.
- REST contract tests for status codes, ADMIN enforcement, multipart validation,
  and safe errors.
- Persistence tests for Flyway schema, foreign keys, state constraints, and
  vector/chunk replacement where the database harness is available.
- Retrieval tests for state, version, scope, and no-result behavior.
- AI boundary tests using mocked assistant/retriever; no live OpenAI key.

## Test Scenario Catalog

### Input boundaries

- PDF with valid extractable text.
- Plain text and Markdown with valid content.
- Empty file.
- Unsupported extension or MIME type.
- Mismatched extension and content type.
- Malformed PDF.
- Content containing Unicode and long lines.

### Lifecycle

- New upload starts as DRAFT.
- DRAFT processes to READY.
- DRAFT processing failure becomes FAILED.
- FAILED can be reprocessed.
- READY can be unpublished.
- DRAFT, FAILED, and UNPUBLISHED can be deleted according to the contract.
- READY cannot be physically deleted directly.
- Invalid transitions return deterministic conflict errors.

### Versioning and indexing

- New version becomes current and prior version becomes inactive.
- Only current READY version is retrieved.
- Reprocessing replaces chunks for the version without duplicates.
- Partial indexing is not visible after failure.

### Authorization and retrieval

- Anonymous and non-ADMIN management requests are rejected.
- Public content is available to permitted chat users.
- Protected scope is unavailable to users without that scope.
- Unpublished, FAILED, DRAFT, and inactive content is excluded.
- No relevant chunk yields no context and safe assistant behavior.

### Operational boundaries

- Embedding provider failure is translated to `FAILED` without leaking details.
- Repository failure does not leave a false READY state.
- Document identifiers are present in safe operational logs.

## Dependency/Version Policy

- Java remains on the repository's Java 21 release line.
- Quarkus remains aligned to the existing 3.39.x platform BOM.
- LangChain4j remains aligned with the Quarkus-managed LangChain4j BOM; do not
  override transitive versions without a compatibility reason.
- PostgreSQL/pgvector remains the vector persistence technology for this phase.
- PDF parsing must use a maintained library compatible with Java 21 and must be
  added with an explicit version in `pom.xml`.
- New dependencies require a security/license review and a focused test.
- No queue, object-storage SDK, antivirus service, or scheduler dependency is
  introduced in the first delivery.

## Implementation Constraints

- Application services depend on ports, not parser, OpenAI, or Panache classes.
- Resources remain thin and do not access repositories directly.
- Flyway owns schema evolution.
- The first implementation may store source content in the database, but the
  storage port must permit a future object-storage adapter.
