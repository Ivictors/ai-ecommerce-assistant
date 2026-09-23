# Engineering Guardrails

## 1. Purpose

These guardrails define boundaries that automated coding agents must respect.

They exist to prevent locally convenient changes from violating domain,
architecture, security, or maintainability requirements.

---

# 2. Specification Guardrails

DO NOT invent material business rules.

DO NOT silently resolve ambiguous requirements.

DO NOT treat existing code as more authoritative than approved specifications.

DO NOT implement a recommended rule as approved behavior without confirming
its status.

DO identify specification impact before implementing significant behavior.

---

# 3. Domain Guardrails

DO NOT expose unrestricted setters for business-critical state merely for
convenience.

Avoid patterns such as:

`order.setStatus(...)`

`payment.setStatus(...)`

when the state transition has business meaning.

DO model meaningful transitions through controlled domain behavior.

DO NOT allow invalid domain states merely because persistence frameworks make
mutable entities easier.

DO NOT move business invariants into REST resources.

DO NOT move business invariants into AI Tools.

DO NOT make repositories responsible for deciding business policy.

---

# 4. Layer Guardrails

Allowed conceptual flow:

Presentation
↓
Application
↓
Domain

Infrastructure supports technical integration.

For AI:

AI Service
↓
Tool
↓
Application
↓
Domain

Avoid:

Resource → Repository

Resource → EntityManager

Resource → external gateway

AI Tool → Repository

AI Tool → EntityManager

AI Tool → database

Domain → REST DTO

Domain → OpenAI

Domain → LangChain4j

---

# 5. LLM Guardrails

The LLM is untrusted for authoritative business state.

Never allow LLM output alone to:

- set a product price;
- modify stock;
- approve payment;
- refund payment;
- change order state;
- authorize access;
- determine ownership;
- approve a return;
- create an arbitrary discount;
- bypass inventory validation.

LLM instructions must never override authenticated user permissions.

Tool arguments originating from an LLM must be validated like any other
untrusted input.

---

# 6. Pricing Guardrails

Never trust prices supplied by:

- frontend;
- customer;
- LLM;
- stale cart state.

ProductVariant base price must come from authoritative backend state.

Promotion rules must be evaluated by trusted application/domain logic.

Order purchase price must be persisted as historical information.

Never recalculate historical order value using current catalog prices.

---

# 7. Inventory Guardrails

Never assume that adding an item to a cart guarantees availability.

Never permit successful reservations beyond available stock.

Concurrency must be considered for inventory mutations.

Reservation and definitive stock consumption must not be treated as identical
operations without an explicit approved model.

Reservation expiration must release availability exactly once.

Repeated processing must not create duplicate inventory effects.

---

# 8. Payment Guardrails

Never trust payment approval from the client.

Only trusted payment integration results may establish external payment
success.

Webhook processing must be idempotent.

Duplicate webhook events must not:

- duplicate payment;
- duplicate order transitions;
- duplicate stock effects;
- duplicate refund effects.

Payment and Order state machines must remain distinct.

A Payment must not receive unrestricted authority to mutate arbitrary Order
state.

---

# 9. Order Guardrails

Order history is immutable with respect to later catalog/customer changes.

Never update historical order price because ProductVariant price changed.

Never update historical order address because Customer address changed.

Never permit arbitrary state transitions.

Validate transitions according to the Order lifecycle.

---

# 10. Authentication and Authorization

Authentication does not imply authorization.

Every protected operation must verify the caller has permission for the
specific resource/action.

A customer must not access another customer's private:

- profile;
- order;
- payment information;
- return;
- cart.

Administrative operations require administrative authorization.

Never rely on frontend route protection as the security boundary.

---

# 11. API Guardrails

REST resources should translate HTTP concerns into application calls.

They should not contain domain workflows.

Do not expose persistence entities as public API contracts when doing so leaks
internal representation or permits unsafe mutation.

Use request/response DTOs where appropriate.

Validate input at the system boundary.

Do not expose internal stack traces to clients.

---

# 12. Persistence Guardrails

Schema changes require Flyway migrations.

Do not rely on automatic schema mutation as the production migration
strategy.

Do not rewrite historical migrations to represent new changes once those
migrations may have been applied.

Use database constraints where they reinforce domain invariants.

Database constraints complement domain validation; they do not replace it.

---

# 13. Transaction Guardrails

Identify transaction boundaries for operations that modify multiple related
pieces of state.

Do not assume external operations participate in the local database
transaction.

Examples:

Database transaction ≠ payment gateway transaction

Database transaction ≠ email delivery

Database transaction ≠ Correios request

Design failure behavior explicitly when local state and external systems
interact.

---

# 14. External Integration Guardrails

External integrations must have defined failure behavior.

Consider:

- timeout;
- retry;
- idempotency;
- unavailable provider;
- malformed response;
- duplicate callback;
- delayed callback.

Do not retry non-idempotent operations blindly.

Do not expose provider-specific implementation details throughout the domain.

---

# 15. Security Guardrails

Never commit secrets.

Never hardcode credentials.

Never log:

- passwords;
- JWTs;
- API keys;
- complete payment/card data;
- private keys.

Minimize personal data exposure in logs.

Treat:

- request parameters;
- headers;
- uploaded content;
- LLM tool arguments;
- webhook payloads

as untrusted input.

---

# 16. Testing Guardrails

Do not remove or weaken a valid test merely to make a build pass.

When behavior intentionally changes:

1. update specification;
2. update expected behavior;
3. update tests;
4. update implementation.

Critical invariants require tests.

Examples:

- cannot reserve unavailable stock;
- duplicate payment webhook does not duplicate effects;
- historical price remains unchanged;
- unauthorized customer cannot access another order;
- invalid Order transition is rejected;
- expired reservation cannot be consumed as active.

---

# 17. Refactoring Guardrails

Do not mix unrelated refactoring with feature implementation unless required.

Refactoring must preserve externally observable behavior unless a specification
explicitly changes that behavior.

Do not introduce:

- microservices;
- Kafka;
- Redis;
- distributed tracing;
- CQRS;
- event sourcing;
- additional databases;
- new architectural layers;

solely because they are considered "best practices."

Introduce complexity only when requirements justify it.

---

# 18. Agent Change Protocol

Before significant code modification, provide or internally establish:

CHANGE
What is being changed?

REQUIREMENT
Which requirement requires it?

DOMAIN OWNER
Which concept owns the behavior?

INVARIANTS
Which rules must remain true?

FILES
Which files are expected to change?

CONTRACT
Does the API or domain contract change?

PERSISTENCE
Is a migration required?

SECURITY
Are authentication/authorization/data boundaries affected?

TESTS
How will the change be verified?

RISKS
What can regress?

Only then implement.

---

# 19. Stop Conditions

Stop and request developer input when:

- two approved requirements conflict;
- an important business rule is missing;
- a change would alter an approved architecture decision;
- a destructive migration is required without an approved migration strategy;
- security requirements are ambiguous;
- financial behavior is ambiguous;
- inventory correctness cannot be guaranteed;
- implementation requires changing an established invariant.

Do not guess through these situations.

---

# 20. Final Validation

Before declaring a task complete:

[ ] Specification respected  
[ ] Domain responsibility respected  
[ ] Invariants preserved  
[ ] Layer boundaries respected  
[ ] Security considered  
[ ] API contract preserved or intentionally updated  
[ ] Migration added if required  
[ ] Tests added/updated  
[ ] Tests pass  
[ ] Build passes  
[ ] No secrets introduced  
[ ] No unrelated changes introduced  
[ ] Documentation updated if necessary