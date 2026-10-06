# Epic Design — AI E-Commerce Assistant MVP

## Architecture Boundaries and System Context

The MVP composes the existing Quarkus backend, PostgreSQL/pgvector persistence,
LangChain4j/OpenAI integration, and Angular client. It adds only the missing
vertical-slice behavior needed for a complete customer support experience,
not new transactional commerce domains.

```text
Administrator
  | protected REST operations: provision/account setup, products, documents
  v
Quarkus REST -> Application -> Domain -> PostgreSQL / pgvector
                         |
Customer                  +-> LangChain4j/OpenAI (chat and embeddings)
  | HTTPS/JWT
  v
Angular -> Auth/session -> REST APIs
  |                         |
  |                         +-> catalog and own-order queries
  |                         +-> create/list own conversations
  |                         +-> structured chat response
  +-> sessionStorage access token
```

The backend remains authoritative for identity, role, ownership, product/order
state, document state, and retrieval scope. The Angular client renders those
results and does not determine business authority.

## Shared Decisions and Cross-Cutting Constraints

- The approved release is an authenticated catalog/order-inquiry and
  knowledge-chat MVP. There is no cart, checkout, payment, stock reservation,
  promotion, return, delivery, or notification workflow in this milestone.
- The client uses the previously approved sessionStorage access-token
  approach; server-side authorization remains mandatory.
- Customer and administrator identities must come from the verified JWT.
- Order and conversation ownership is evaluated by the backend; client-supplied
  owner identifiers are never authoritative.
- Chat keeps the approved versioned response envelope and requires a valid,
  owned conversation identifier.
- Knowledge documents continue to be administered through protected REST APIs;
  an Angular administration console is outside the approved frontend scope.
- Flyway owns database changes; Hibernate schema validation checks mappings.
- Refresh-token lifecycle and password recovery remain separate #42/#43 work
  and are not required for the local learning MVP. They must be reassessed
  before public production use.

## Current System Gaps Confirmed

- #44: seeded/existing accounts may have no password hash, so a safe account
  credential-provisioning route is required before real user login can be
  exercised.
- #47: conversation listing and chat exist, but no API creates a conversation;
  an Angular client otherwise cannot obtain a valid identifier.
- #36–#41: the Angular app has service/auth foundations but not the complete
  customer screens and end-to-end validation.
- #13 was narrowed to observability for existing components; payment/webhook
  metrics must not be represented as implemented capability.

## Global Risks and Mitigation

- **Initial administrator bootstrap:** The first privileged account cannot be
  created by an already-protected ADMIN operation. #44 must resolve a secure
  bootstrap authority before implementation; no public unauthenticated
  provisioning endpoint is assumed.
- **LLM nondeterminism/provider dependency:** test API/resource behavior with
  controlled substitutes where possible; retain application-backed product,
  order, authorization, and document rules.
- **Empty local knowledge data:** provide controlled test documents for the
  RAG acceptance flow; do not treat absence of production data as evidence of
  retrieval quality.
- **Scope creep toward checkout:** keep transactional domains out until a
  separately approved specification creates bounded issues.
- **Short-lived access token UX:** without refresh tokens, users will need to
  authenticate again after expiry; this is accepted for the MVP and tracked in
  #42.

## Epic-Level Open Questions

- **Q-001 (deferred to #44):** What trusted operator/bootstrap mechanism
  establishes the first ADMIN credential?
  - Rationale: This is a security design decision for credential provisioning,
    not a product-scope decision. It must be resolved before implementing #44
    and before accepting the user journey as fully usable.
- **Q-002 (deferred beyond MVP):** What transactional domains and provider
  integrations should be implemented after this MVP?
  - Rationale: Cart, checkout, payment, stock, promotions, returns, delivery,
    and notifications are expressly outside the approved MVP. Create their
    issues only after new product requirements and business decisions are
    approved.

## Exit Conditions

- #44 and #47 are complete before the Angular journey that depends on them.
- #36–#41 pass end-to-end against the approved REST contracts.
- All MVP acceptance criteria trace to tests and existing/added code.
- #13–#15 provide an evidence-based local operational and hardening review.
- Deferred commerce and production-auth capabilities remain explicitly open,
  not represented as delivered.
