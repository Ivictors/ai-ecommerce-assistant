# Spec-Driven Development

## 1. Objective

This project follows Spec-Driven Development.

Specifications define intended system behavior before implementation details.

Code is an implementation of approved decisions, not the primary source from
which business behavior should be inferred.

---

## 2. Development Flow

Every significant feature follows:

Requirement
↓
Business Rule
↓
Domain Responsibility
↓
Invariants
↓
Domain Model
↓
Persistence Impact
↓
API Contract
↓
Implementation Plan
↓
Implementation
↓
Tests
↓
Validation

Do not skip directly from:

"Feature request"
↓
"Generate Entity + Repository + Service + Resource"

The domain must first be understood.

---

## 3. Before Coding

For every non-trivial change, answer:

### Requirement

What user or business requirement requires this change?

### Business Rule

What behavior must always be respected?

### Responsibility

Which domain concept owns this behavior?

### Invariant

What condition must never become invalid?

### State Transition

Does the operation change domain state?

If yes:

FROM → EVENT → TO

must be identified.

### Persistence

Does the change require new persisted state?

### Contract

Does the external API change?

### Security

Who may perform this operation?

### Failure

What can fail and what should happen when it fails?

### Tests

Which scenarios demonstrate that the rule works?

---

## 4. Domain First

Framework capabilities must not dictate the domain model.

Do not design a domain concept merely because:

- Hibernate makes it convenient;
- Panache provides a particular API;
- REST expects a certain DTO;
- LangChain4j expects a Tool;
- PostgreSQL has a convenient representation.

Start from business meaning.

Technology adapts to the model where practical.

---

## 5. Business Invariants

An invariant is a condition that must remain true after every valid business
operation.

Examples in this project include:

- stock cannot be reserved beyond availability;
- an order cannot arbitrarily transition between states;
- historical order prices cannot change when catalog prices change;
- payment state cannot be invented by the frontend or LLM;
- an authenticated customer cannot access another customer's private order;
- a promotion must not overwrite the variant's base price.

Every new feature must identify affected invariants.

---

## 6. State Machines

Concepts with meaningful lifecycle must have explicit valid transitions.

Examples include:

- Order;
- Payment;
- Reservation;
- Return.

Do not expose generic state mutation such as:

`setStatus(...)`

when transitions have business meaning.

Prefer behavior representing intent.

Conceptually:

`approvePayment()`

is preferable to:

`setStatus(APPROVED)`

when approval contains business rules.

Exact method names are implementation decisions, but the principle must be
preserved.

---

## 7. Unknown Requirements

Agents must distinguish:

### Known

Explicitly approved behavior.

### Derived

Behavior necessarily implied by approved requirements.

### Recommended

A proposed decision that has not yet been approved.

### Unknown

Information that cannot safely be determined.

Do not convert `Recommended` or `Unknown` decisions into permanent business
behavior without approval when they materially affect the system.

---

## 8. Change Protocol

When changing existing behavior:

1. identify the existing specification;
2. identify the requested change;
3. determine affected invariants;
4. determine affected domain concepts;
5. determine API compatibility impact;
6. determine persistence impact;
7. determine migration requirements;
8. update specification;
9. update tests;
10. update implementation;
11. validate.

---

## 9. AI-Assisted Development

AI-generated code is subject to the same engineering process.

The agent must not use code generation as a substitute for modeling.

For substantial changes, the agent should explain:

- intended change;
- affected responsibility;
- files expected to change;
- risks;
- validation strategy.

The developer remains responsible for unresolved architecture and business
decisions.

---

## 10. Completion

A specification is considered implemented only when:

Specification
↓
Implementation
↓
Automated verification
↓
Build validation

agree with each other.

If they disagree, the work is not complete.