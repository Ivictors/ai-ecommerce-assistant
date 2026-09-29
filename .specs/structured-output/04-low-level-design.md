# Low-Level Design — Selective Structured Output

## Public Response Contract

`POST /api/chat` returns `application/json`.

Common envelope:

```json
{
  "type": "TEXT",
  "schemaVersion": "1",
  "message": "How can I help?",
  "data": null
}
```

`type` is a closed discriminator:

- `TEXT` — general conversation;
- `PRODUCT_INFORMATION` — authoritative product information;
- `ORDER_STATUS` — owner-validated order information;
- `POLICY_INFORMATION` — approved RAG knowledge;
- `FALLBACK` — clarification or insufficient trusted information.

### Product payload

```json
{
  "id": 10,
  "name": "Notebook",
  "description": "...",
  "price": 3999.90,
  "stock": 4,
  "active": true
}
```

### Order payload

```json
{
  "id": 25,
  "status": "PAID",
  "total": 3999.90,
  "createdAt": "2026-09-29T12:00:00Z"
}
```

The public response does not expose `userId`; ownership is enforced internally.

### Policy payload

```json
{
  "content": "The return period is seven days."
}
```

Source identifiers remain internal in the first version.

### Error responses

Existing API error mapping remains in force. Malformed requests use
`INVALID_REQUEST`; authorization and ownership retain the existing security
semantics. Invalid model output must become a `FALLBACK`, not an HTTP 500.

## Classification Contract

The current `UserIntent` is replaced or adapted to a controlled representation:

```text
PRODUCT_INFORMATION
ORDER_STATUS
POLICY_INFORMATION
UNKNOWN
```

`productName` is optional for non-product intents. Missing required values do
not authorize a lookup; they produce clarification or `UNKNOWN` fallback.

## Validation Rules

- `type` and `schemaVersion` are always present.
- `message` is always present and safe for the user.
- `data` matches the discriminator or is null.
- Product values are mapped from `ProductService`/`ProductTool` results.
- Order values are mapped after authenticated ownership validation.
- Policy content is emitted only when the retriever returns approved context.
- Unknown or malformed classification never selects an unapproved route.

## Test Strategy

- DTO serialization tests for every response type.
- Classification parser tests for valid, unknown, missing, and incompatible
  values.
- Application routing tests with mocked authoritative services.
- Contract tests for envelope fields, discriminator/data consistency, and
  schema version.
- Security tests confirming ownership and role behavior remain enforced.
- No live LLM or OpenAI key required.

## Dependency and Version Policy

- Use existing Java 21 and Quarkus 3.39.x platform versions.
- Use existing LangChain4j-managed versions.
- Do not add a JSON schema runtime dependency unless the implementation shows
  a concrete need; Jackson records and validation are sufficient initially.
- Do not expose persistence entities as response contracts.

## Tasks

### T-001: Define response envelope and discriminated payload DTOs

- AC-IDs: AC-001, AC-002, AC-003, AC-004, AC-005, AC-009
- Test-IDs: T-001-T1 (serialization), T-001-T2 (data/discriminator consistency)
- Files: `presentation/rest/chat/*Response.java`, matching tests
- Dependencies: none
- Gates: unit, contract
- Rollback: revert commit; no database change

### T-002: Define controlled intent and parser validation

- AC-IDs: AC-007, AC-008
- Test-IDs: T-002-T1 (valid intents), T-002-T2 (invalid/incomplete intent)
- Files: `infrastructure/ai/dto/UserIntent.java`, intent enum/parser/tests
- Dependencies: none
- Gates: unit
- Rollback: revert commit; no API migration

### T-003: Define application routing and fallback contract

- AC-IDs: AC-002, AC-006, AC-007, AC-010
- Test-IDs: T-003-T1 (TEXT), T-003-T2 (policy fallback), T-003-T3 (authorization preservation)
- Files: `application/chat/*`, fallback types/tests
- Dependencies: T-001, T-002
- Gates: unit, security
- Rollback: revert commit; existing chat behavior can be restored

### T-004: Define API compatibility and integration tests

- AC-IDs: AC-001, AC-009, AC-010
- Test-IDs: T-004-T1 (REST response contract), T-004-T2 (security/ownership)
- Files: ChatResource tests, API documentation, implementation log
- Dependencies: T-003
- Gates: REST, security
- Rollback: revert commit; no migration

## Open Questions

None. The single JSON envelope and selective scope were approved in the product
specification and high-level design.
