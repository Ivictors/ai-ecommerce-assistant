# High-Level Technical Design — Selective Structured Output

## Architecture Diagram

```text
Client
  |
  v
ChatResource
  |
  v
ChatApplicationService
  |
  +--> EcommerceAssistant.classify()  [untrusted intent]
  |
  +--> IntentValidator / Router
          |
          +--> General conversation -> assistant.chat() -> TextResponse
          +--> Product flow -> ProductService/Tool -> ProductResponse
          +--> Order flow -> OrderService/Tool -> OrderResponse
          +--> Policy flow -> KnowledgeRetriever -> PolicyResponse
          +--> Invalid/no-result -> FallbackResponse
  |
  v
Common JSON ChatResponse envelope
```

## Component Responsibilities

### `ChatResource`

- Accepts the authenticated chat request.
- Returns `application/json`.
- Performs transport validation only.
- Does not interpret model output or build business data.

### `ChatApplicationService`

- Resolves conversation ownership.
- Coordinates classification and routing.
- Calls authoritative application capabilities for typed flows.
- Builds the common response envelope.
- Applies safe fallback behavior.

### `IntentValidator`

- Converts model classification into a controlled application intent.
- Rejects or normalizes unsupported values according to the low-level contract.
- Never grants authorization or determines ownership.

### Response factories

- `TextResponseFactory` creates free-form responses.
- `ProductResponseFactory` maps authoritative product data.
- `OrderResponseFactory` maps owner-validated order data.
- `PolicyResponseFactory` maps retrieved approved knowledge.
- `FallbackResponseFactory` creates clarification or insufficient-knowledge
  responses.

### Existing capabilities

- `ProductService` and `ProductTool` remain authoritative for product state.
- `OrderService` and `OrderTool` remain authoritative for order state.
- `KnowledgeRetriever` remains authoritative for approved policy context.
- Security and ownership services remain server-side boundaries.

## Response Boundary

Every response uses one envelope discriminator:

```json
{
  "type": "TEXT | PRODUCT_INFORMATION | ORDER_STATUS | POLICY_INFORMATION | FALLBACK",
  "schemaVersion": "1",
  "message": "Human-readable response",
  "data": {}
}
```

The `data` payload is present only for the applicable typed response. The exact
payload contracts are defined in issue #28. The model never directly supplies
authoritative fields in `data`.

## Data Flow

1. The resource validates the request and delegates to the application.
2. The application resolves the authenticated conversation owner.
3. The assistant classifies the message into `UserIntent`.
4. The validator accepts only supported intents and required fields.
5. The router selects a response flow.
6. The selected application capability obtains authoritative data.
7. A response factory creates the typed payload and common envelope.
8. Invalid, incomplete, or insufficient data produces a safe fallback.

General conversation may continue to use the assistant's natural-language
response, but it is wrapped as `type=TEXT`.

## API Contract Sketch

`POST /api/chat`

Request remains authenticated and conversation-owned. Response content type
becomes `application/json`.

Common fields:

- `type` — response discriminator;
- `schemaVersion` — public contract version;
- `message` — user-facing natural-language message;
- `data` — optional typed payload.

The first version uses one endpoint and one envelope version. Version changes
must be deliberate and tested.

## Security Posture

- The LLM classification is untrusted input.
- `UserIntent` cannot authorize operations or override roles.
- Product and order data are loaded through existing secured application paths.
- Order responses require authenticated ownership checks.
- The response mapper must not expose another customer's data.
- Structured output must not expose tokens, internal prompts, stack traces, or
  private document content.

## Observability

The first implementation may record response type and fallback reason without
logging the full user message, token, prompt, or private data. Detailed metrics
and dashboards remain under issue #13.

## Trade-offs

### One JSON envelope

This keeps the API surface small and allows text and typed responses to coexist.
It changes the current `text/plain` response contract, but avoids maintaining
parallel chat endpoints.

### Application-built typed payloads

The backend performs mapping after authoritative service calls. This adds
application code but prevents the LLM from inventing transactional values.

### Schema version in every response

The field adds minor payload overhead but makes future contract evolution
explicit for Angular clients.

## Risks and Rollback

- Existing clients expecting plain text may fail after the content-type change;
  update the client contract before enabling the new endpoint behavior.
- Invalid model classifications can route incorrectly; validate before routing.
- A future response field may accidentally expose persistence details; use DTOs,
  never entities.
- Rollback can restore the previous text response only if the client contract
  is versioned or the deployment is reverted together.

## Test Scenario Map

- Valid general conversation returns `TEXT`.
- Valid product intent returns authoritative product data.
- Valid order intent returns only the authenticated owner's order data.
- Valid policy intent returns approved RAG content.
- No RAG context returns safe fallback.
- Unknown or incomplete intent returns fallback/clarification.
- Model-provided price/status conflicting with backend data is ignored.
- Response JSON always contains type, schema version, and message.
- Existing authentication and conversation ownership tests remain green.

## Open Questions

None. Response field details and exact validation rules are implementation
contracts for issue #28.

## Resolved Questions

- One endpoint with a JSON envelope was approved in issue #26.
- Selective structured output was approved in issue #26.
