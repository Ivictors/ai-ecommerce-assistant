# Existing Codebase Onboarding

Assessment date: 2026-09-23
Scope: `ai-ecommerce-assistant`, currently focused on `backend/ai-ecommerce-assistant/`.

## 1. Classification and structure

- Type: brownfield; code, database migrations, tests, and feature commits already exist.
- Shape: single repository with a Quarkus backend and infrastructure files at the root.
- Backend: Java 21/Quarkus application built with the Maven Wrapper.
- Frontend: planned by the documents and roadmap, but no `frontend/package.json` or Angular source exists currently.
- Database: PostgreSQL using `pgvector/pgvector:pg16` in `docker-compose.yml`.
- Migrations: Flyway under `backend/ai-ecommerce-assistant/src/main/resources/db/migration/`.
- Development entry point: `backend/ai-ecommerce-assistant/mvnw.cmd quarkus:dev` on Windows.
- Main validation: `backend/ai-ecommerce-assistant/mvnw.cmd test`.

## 2. Current architecture

The code partially follows the separation defined in `AGENTS.md`:

- `domain`: entities and business states such as `Order`, `Product`, `Conversation`, and `User`.
- `application`: use-case coordination such as `OrderService`, `ConversationService`, `ChatApplicationService`, and `CurrentUserService`.
- `infrastructure`: Panache persistence, LangChain4j/OpenAI integration, and Tools.
- `presentation/rest`: HTTP resources, DTOs, and transport-level validation/access control.

Observed flows:

- Orders: REST → application → repository, with filtering by authenticated user in `OrderService`.
- Chat: REST → `ChatApplicationService` → `EcommerceAssistant`; the duplicate infrastructure REST resource was removed.
- AI: `EcommerceAssistant` uses LangChain4j memory and product/order Tools.
- Persistence: JPA/Panache entities and repositories under `infrastructure/persistence`.
- Security: SmallRye JWT is a dependency; `CurrentUserService` reads the principal as a numeric identifier.

## 3. Conventions to preserve

- Java 21 and four-space indentation.
- One public class per file.
- `*Resource`, `*Service`, `*Repository`, `*Request`, and `*Response` naming.
- Thin REST resources delegating rules to application/domain layers.
- JUnit 5 and Mockito tests organized by matching package.
- Versioned Flyway migrations; never edit migrations that may already be applied.
- Never trust the frontend or LLM for price, stock, payment, authorization, or ownership.
- Do not expose persistence entities directly when DTOs are required.

## 4. Integrations and entry points

- PostgreSQL/PGVector through Docker Compose.
- Flyway startup migration according to the example configuration.
- LangChain4j/OpenAI through `quarkus-langchain4j-openai`.
- JWT through `quarkus-smallrye-jwt`.
- REST through Quarkus REST/Jackson.
- Hibernate ORM Panache for persistence.
- No messaging, CI/CD, or implemented frontend was identified.

## 5. Quality baseline

Command executed on 2026-09-23:

```text
backend/ai-ecommerce-assistant/.\mvnw.cmd test
```

Result: 10 tests executed, 10 passed, 0 failures, 0 errors.

The build reported Mockito/Byte Buddy self-attach warnings. They did not fail the build but should be monitored as future JDKs restrict dynamic agent loading.

No lint, formatter, coverage, SpotBugs, Checkstyle, or GitHub Actions configuration was found.

## 6. Risks, debt, and unknowns

- Authentication and authorization are not complete for chat, conversations, or administrative operations.
- The JWT principal-to-`User` resolution contract needed specification and validation.
- `ConversationResource` accepts a client-supplied `userId`; ownership must be enforced through the authenticated identity.
- `ProductResource` operations needed an administrative boundary.
- No standardized HTTP error contract exists yet.
- Conversation creation and message persistence are not defined.
- JPA entities also serve as part of the domain model; refactoring requires an architectural decision.
- Example configuration contains placeholders and must never contain real secrets.
- No REST integration tests or automated database/Testcontainers setup was identified.
- Code coverage is not measured.
- Angular is planned but absent.
- The README remains the initial Quarkus template and does not describe the domain, security, Docker, or SLDD workflow.

## 7. Workflow gaps

- [ ] Create `.specs/<feature-id>/` artifacts for each feature.
- [ ] Define a database integration baseline and decide on Testcontainers.
- [ ] Add lint/format/quality gates only after project approval.
- [ ] Add CI for approved commands.
- [ ] Create a requirement/task/test/code traceability matrix.
- [ ] Formalize error, security, and ownership contracts before RAG.

## 8. Context for subsequent work

The next work unit is authentication and authorization. It must begin with product intent and acceptance criteria, proceed through review and design, and only then generate tests and implementation. The current unit-test baseline is green, but it does not represent complete security, database, REST, or OpenAI integration validation.
