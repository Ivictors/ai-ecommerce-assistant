# Product Intent Specification — AI E-Commerce Assistant MVP

## Source

- Tracker: GitHub
- ID: #46 — `[SLDD] Decide MVP scope and plan remaining commerce workflows`
- URL: https://github.com/Ivictors/ai-ecommerce-assistant/issues/46
- Snapshot date: 2026-10-05
- Approval source: user message on 2026-10-05 — “aprovado. quero que implemente seguindo sua recomendação. todos os passos - vamos optar por um MVP.”

## Goal

Deliver a usable first release in which an authenticated customer can browse
the existing product catalog, inspect only their own orders, start a
conversation, and receive structured assistant responses grounded in approved
product, order, or knowledge data. Administrators can prepare products and
knowledge documents through the existing protected backend APIs. This MVP is an
AI-assisted catalog and customer-support experience, not a transactional store.

## Target Users

- Customer with an account and credentials provisioned through the approved
  administrative workflow.
- Administrator who maintains products and approved knowledge documents using
  protected backend APIs.

## Success Metrics

- The acceptance scenarios below pass through the Angular UI and the real
  Quarkus/PostgreSQL backend without production credentials.
- A customer can complete the full login → catalog/order inquiry → start
  conversation → structured chat journey using only their authenticated
  identity.
- An administrator can upload and process approved knowledge documents, and
  the assistant can answer an in-scope policy question from indexed content.
- No cart, checkout, payment, or other out-of-scope transactional action is
  presented as available in the MVP.

## Acceptance Criteria (EARS-lite)

- AC-001: When a customer submits valid provisioned credentials, the system
  shall establish the approved authenticated session using the backend-issued
  access token.
- AC-002: When an authenticated customer opens the catalog, the system shall
  display products and authoritative product values returned by the backend.
- AC-003: When an authenticated customer requests their orders, the system
  shall return only orders owned by the authenticated identity.
- AC-004: When an authenticated customer starts a conversation, the system
  shall create a conversation owned by the authenticated identity and return
  its identifier.
- AC-005: When a customer sends a chat message for an owned conversation, the
  system shall return the approved structured response envelope and render the
  result according to its response type and schema version.
- AC-006: When an administrator uploads and processes an approved knowledge
  document, the system shall make only successfully processed content in the
  approved READY/current-version state available to its retrieval scope.
- AC-007: When a customer asks a policy question and relevant approved content
  exists, the assistant shall produce a policy response using that retrieved
  content.
- AC-008: If no relevant approved policy content exists, then the assistant
  shall return the approved insufficient-knowledge fallback instead of
  asserting an unsupported policy.
- AC-009: When a customer requests product or order information through chat,
  the system shall source authoritative values from backend application data
  and return the corresponding typed response.
- AC-010: If a customer requests an order or conversation owned by another
  identity, then the system shall not disclose that resource and shall return
  the approved not-found behavior.
- AC-011: When an unauthenticated caller attempts a protected customer or
  administrator operation, the system shall reject the request server-side.
- AC-012: When an administrator performs product or knowledge-document
  management through the backend API, the system shall enforce the existing
  administrator authorization rules.

## Non-Goals

- Cart creation, checkout, order creation, payment, refunds, or payment
  provider/webhook integration.
- Product variants, stock reservation/consumption, promotion calculation,
  returns, delivery fulfillment, and customer notifications.
- Guest sessions, self-service registration, or a frontend credential
  provisioning screen.
- Angular administration screens for product or knowledge-document
  management; administrators use the protected backend API in this MVP.
- Refresh tokens and password recovery; these remain tracked by #42 and #43
  and must be revisited before a public production release.
- Public deployment or a production availability/performance commitment.

## Non-Functional Requirements

- Existing security guardrails remain mandatory: authorization is enforced by
  the backend, ownership comes from the authenticated identity, and secrets,
  credentials, and tokens are not logged.
- No quantitative latency, throughput, or availability target has been
  approved for this learning MVP. Such targets are deferred to production
  planning and are not silently invented here.
- Local validation uses the repository-provided PostgreSQL/pgvector setup and
  test-only credentials/keys; production secrets are not required.

## Glossary

- **MVP** — The first deliberately bounded, usable product release.
- **Customer** — An authenticated user with the `USER` role.
- **Administrator** — An authenticated user with the `ADMIN` role.
- **Approved knowledge document** — An internally supplied document that an
  administrator has uploaded, processed successfully, and published under an
  explicit knowledge scope.
- **Structured response envelope** — The versioned chat response contract
  containing a response type, message, and optional typed data.
- **Authoritative value** — Product/order state sourced from backend
  application data, not inferred by the language model or submitted as truth
  by the client.
- **Transactional e-commerce** — A user journey that changes purchase state,
  such as cart, checkout, payment, stock reservation, or order creation.

## Risks and Assumptions

- Issue #44 must establish a safe way to provision at least one administrator
  and customer account; existing users may have no password hash.
- Issue #47 is required because conversations can be listed and used by chat,
  but there is currently no REST operation to create one.
- Current backend knowledge-document management remains API-only in this MVP.
- The database may require controlled test fixtures for catalog, orders, and
  knowledge content; fixtures must not become production credentials.
- The Angular and backend contracts may expose integration gaps that must be
  resolved in their own bounded issues before declaring this MVP complete.

## Open Questions

None about the approved product scope. Security mechanics for initial
administrator credential provisioning remain a decision gate owned by issue
#44, not an implicit choice in this specification.

## Resolved Questions

- **Q-001:** Is the first complete release a transactional e-commerce flow or
  an AI-assisted catalog/order-inquiry MVP?
  - Resolution: “aprovado. quero que implemente seguindo sua recomendação.
    todos os passos - vamos optar por um MVP.” The first release is the
    AI-assisted catalog/order-inquiry MVP; transactional commerce is deferred.
  - Resolved at: 2026-10-05
- **Q-002:** Is Angular administration for product and knowledge-document
  management part of this MVP?
  - Resolution: Existing frontend scope in issue #12 covers customer catalog,
    authentication, order inquiry, conversations, and structured chat, but no
    administration UI. To keep this approved MVP bounded, administrators use
    the existing protected backend APIs; a management UI is a non-goal.
  - Resolved at: 2026-10-05
