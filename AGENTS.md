# AI E-Commerce Assistant — Agent Guidelines

## 1. Purpose

This repository contains an AI-assisted e-commerce platform built with Java 21,
Quarkus, PostgreSQL/pgvector, LangChain4j, OpenAI, Flyway, Docker, and an Angular
frontend.

This is not a CRUD tutorial project.

The project is intended to model a realistic e-commerce domain while applying
software engineering practices such as:

- requirements-driven development;
- explicit domain modeling;
- separation of responsibilities;
- business invariant protection;
- API contracts;
- automated testing;
- security by design;
- observability;
- controlled AI integration.

Agents working in this repository must preserve these principles.

---

## 2. Sources of Truth

Before implementing or modifying behavior, read:

1. `specs/SPEC_DRIVEN_DEVELOPMENT.md`
2. `specs/CONTRACT.md`
3. `specs/GUARDRAILS.md`
4. the specification related to the feature being modified;
5. existing tests;
6. existing implementation.

Priority when information conflicts:

1. approved business requirements;
2. domain rules and invariants;
3. approved architectural decisions;
4. API contracts;
5. tests representing approved behavior;
6. current implementation;
7. agent assumptions.

Existing code is not automatically authoritative.

If implementation contradicts an approved specification, do not preserve the
incorrect behavior merely because it already exists.

---

## 3. Spec-Driven Development

This project follows:

Requirements
↓
Business Rules
↓
Domain Model
↓
DER / Persistence Model
↓
API Contract / OpenAPI
↓
Physical Architecture
↓
Implementation
↓
Tests
↓
Validation
↓
Deployment

Do not reverse this process by inventing domain rules from implementation
convenience.

Before implementing a feature, determine:

1. Which requirement is being implemented?
2. Which business rule applies?
3. Which domain concept owns the rule?
4. Which invariants must remain true?
5. Which API contract is affected?
6. Which persistence structures are affected?
7. Which security boundaries are affected?
8. Which tests prove the expected behavior?

If the specification does not answer an important business question, do not
silently invent the answer.

---

## 4. Human-in-the-Loop Engineering

The agent is an engineering assistant, not the project owner.

When a requirement or architectural decision is unresolved:

1. identify the unresolved decision;
2. explain why it matters;
3. present viable alternatives;
4. explain their trade-offs;
5. provide a recommendation when useful;
6. wait for developer approval when the choice materially changes business
   behavior or architecture;
7. update the appropriate specification after approval;
8. implement only after the decision is established.

Do not silently introduce new business rules.

---

## 5. Project Structure

The Quarkus backend is located at:

`backend/ai-ecommerce-assistant/`

Application packages are under:

`src/main/java/com/victor/ecommerce/`

Primary responsibilities:

### `domain`

Contains domain concepts, business state, domain behavior, invariants,
value objects, and domain enums.

The domain must not depend on:

- REST;
- HTTP;
- JSON;
- LangChain4j;
- OpenAI;
- Panache-specific transport concerns;
- presentation DTOs.

### `application`

Coordinates use cases and application workflows.

Responsibilities include:

- orchestration;
- transaction boundaries;
- calling domain behavior;
- coordinating repositories;
- coordinating external ports;
- authorization-related application workflows where appropriate.

Application services must not duplicate business invariants already owned by
the domain.

### `infrastructure`

Contains technical integrations such as:

- persistence;
- PostgreSQL;
- pgvector;
- LangChain4j;
- OpenAI;
- external providers;
- repository implementations;
- infrastructure configuration.

Infrastructure adapts technology to the application/domain.

It must not become the owner of business rules.

### `presentation/rest`

Contains:

- REST resources;
- HTTP DTOs;
- request validation related to transport;
- HTTP status mapping;
- API error representation.

REST resources should remain thin.

They must not contain core business logic.

---

## 6. Dependency Direction

Preferred dependency direction:

Presentation
↓
Application
↓
Domain

Infrastructure implements technical capabilities required by the application
and domain boundaries.

Avoid shortcuts such as:

Resource → Repository

Resource → Database

Domain → REST

Domain → LangChain4j

Domain → OpenAI

AI Tool → Database

LLM → Repository

---

## 7. AI Integration

The LLM is not a source of truth for business state.

LangChain4j AI Services and Tools are adapters into existing application use
cases.

Expected flow:

User
↓
AI Service
↓
Tool
↓
Application Service
↓
Domain
↓
Repository / external port

Tools must not bypass the application layer to modify business state.

The LLM must never be authoritative for:

- product prices;
- stock quantities;
- promotion eligibility;
- payment status;
- order status;
- authorization;
- customer ownership;
- refunds;
- returns;
- inventory reservation.

These values must come from deterministic application/domain behavior and
trusted data sources.

---

## 8. Repository Conventions

Use:

- Java 21;
- four-space indentation;
- one public type per file;
- descriptive names;
- explicit responsibilities.

Naming conventions:

- `*Resource` — HTTP resources;
- `*Service` — application services;
- `*Repository` — repository abstractions/implementations according to package;
- `*Request` — incoming API DTOs;
- `*Response` — outgoing API DTOs;
- domain names — business concepts rather than framework terminology.

Do not create generic abstractions without a concrete need.

Prefer explicit domain language over generic names such as:

- `Manager`;
- `Helper`;
- `Util`;
- `Processor`;

unless the abstraction genuinely represents that responsibility.

---

## 9. Database and Flyway

Flyway migrations are located at:

`src/main/resources/db/migration/`

Database changes must be represented through versioned migrations.

Never modify an already-applied production migration to represent a new schema
change.

Before creating a migration, verify that the persistence change follows an
approved domain decision.

Database constraints should reinforce important invariants when appropriate,
but database constraints do not replace domain rules.

---

## 10. Testing

Tests belong under the matching:

`src/test/java`

package structure.

Test classes should follow:

`*Test.java`

New behavior should include tests at the appropriate level.

Prioritize tests for:

1. domain invariants;
2. application use cases;
3. security/authorization boundaries;
4. REST contracts;
5. persistence behavior;
6. external integration adapters where appropriate.

Do not create tests merely to increase coverage.

Tests should demonstrate behavior.

Before considering work complete, run:

`./mvnw test`

When appropriate also run:

`./mvnw clean verify`

---

## 11. Build and Development

Run backend commands from:

`backend/ai-ecommerce-assistant/`

Development:

`./mvnw quarkus:dev`

Windows:

`mvnw.cmd quarkus:dev`

Tests:

`./mvnw test`

Validation:

`./mvnw clean verify`

Package:

`./mvnw package`

From repository root, PostgreSQL/pgvector can be started with:

`docker compose up -d`

Required database configuration must be supplied through environment variables
or approved local configuration.

---

## 12. Security

Never commit:

- OpenAI API keys;
- passwords;
- JWT private keys;
- access tokens;
- production database credentials;
- `.env` secrets;
- private certificates.

Never log:

- passwords;
- authentication tokens;
- complete payment/card information;
- secrets;
- unnecessary personal data.

Client-provided values must not be trusted for authoritative business
information such as price, discount, stock, ownership, or payment state.

Authorization must be enforced server-side.

---

## 13. Git

Use Conventional Commits.

Examples:

`feat: add inventory reservation`

`fix: prevent reservation above available stock`

`test: add reservation expiration scenarios`

`refactor: separate promotion calculation`

`docs: document order lifecycle`

Keep unrelated changes in separate commits.

Before suggesting a commit, explain what changed and why.

---

## 14. Definition of Done

A feature is not complete merely because it compiles.

Before declaring work complete verify:

- requirement satisfied;
- domain invariant preserved;
- architecture respected;
- API contract respected;
- authorization considered;
- persistence migration included when necessary;
- tests added or updated;
- tests passing;
- build passing;
- documentation/specification updated when behavior changed;
- no secrets introduced;
- no unrelated changes included.

Compilation success is a checkpoint, not the definition of done.
