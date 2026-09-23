# Design Técnico Epic — Autenticação e Autorização

Feature: `.specs/security-authorization/01-spec.md`
Review: `.specs/security-authorization/02-spec-review.md`

## Architecture boundaries and system context

```text
HTTP request
    |
    v
Quarkus REST Resource
    |  authentication boundary: JWT verification / SecurityIdentity
    v
Application security boundary
    |-- CurrentUserService: resolve authenticated numeric subject
    |-- Authorization policy: role and ownership decisions
    v
Application use case
    |-- OrderService
    |-- ConversationService / ChatApplicationService
    |-- ProductService
    v
Domain + repositories
    |
    v
PostgreSQL / trusted external adapters

AI Service -> AI Tool -> Application use case -> authorization + domain rules
```

The REST layer translates transport concerns. The application layer owns the orchestration of identity, ownership and use-case authorization. Domain objects remain responsible for business invariants. Infrastructure provides JWT, persistence and LangChain4j adapters, but does not become the owner of authorization policy.

## Shared decisions and cross-cutting constraints

- The verified JWT `sub` is the numeric identifier of the persisted `User`.
- The verified JWT `role` claim identifies the administrator with value `ADMIN`.
- Ownership is always evaluated from the authenticated identity, never from a client-provided owner identifier.
- Private resources belonging to another customer are represented as `404 Not Found`.
- Current public and protected endpoints follow the matrix in `01-spec.md`.
- AI Tools call application use cases and cannot access repositories or databases directly.
- The error response format is a dependency on the error-contract issue and must be stable before final implementation.
- No new database schema is required for the authorization boundary unless the approved low-level design identifies a missing persisted role or identity mapping.

## Component responsibilities

| Component | Responsibility | Must not own |
|---|---|---|
| Quarkus JWT layer | Verify token signature/claims and expose authenticated identity | Resource ownership policy |
| `SecurityIdentity` adapter | Expose verified principal and roles to application | Trust unverified client data |
| `CurrentUserService` | Resolve numeric `sub` and authenticated state | Decide resource-specific ownership |
| Authorization policy/application services | Enforce role and resource ownership before use case execution | JWT cryptography or persistence internals |
| REST resources | Declare endpoint boundary and map HTTP concerns | Core business rules or direct repository access |
| Domain entities | Preserve business invariants and valid state | HTTP/JWT/LangChain4j concerns |
| AI Tools | Translate model requests into approved application calls | Direct database access or permission bypass |
| Repositories | Load/persist data using approved filters | Invent authorization policy |

## Data flow

1. Quarkus receives the request.
2. JWT verification establishes an authenticated or anonymous `SecurityIdentity`.
3. The REST boundary applies endpoint-level authentication/role requirements.
4. The application service resolves the numeric authenticated user from the verified `sub`.
5. The application use case loads the requested resource with an ownership-aware query or validates ownership before continuing.
6. The domain operation executes only after authorization succeeds.
7. The response is mapped to the approved API contract; authorization failures use the approved error representation.

For AI:

1. The authenticated user sends a message.
2. The AI Service may select a Tool.
3. The Tool passes the request to an application use case.
4. The application use case derives identity from the authenticated context and validates ownership/role.
5. Only then can the use case read or mutate protected state.

## Data model overview

- `User` is the persisted customer identity.
- `Conversation` references a `User` and therefore has customer ownership.
- `Order` references a customer through its existing model and must be queried by authenticated owner.
- Product mutation operations require the administrator role; product catalog visibility remains public in this milestone.
- The current design does not add a role field to `User` because the approved requirement identifies the administrator through the verified JWT `role` claim. This must be revisited if the identity provider or persistence model changes.
- Existing Flyway migrations remain unchanged unless the low-level design finds a required schema change.

## Security posture

- Authentication is server-side and based on verified JWT credentials.
- Authorization is enforced at the backend and repeated at application boundaries for protected use cases.
- Frontend route guards are not security controls.
- LLM output and Tool arguments are untrusted input.
- Logs and errors must not expose tokens, credentials, private keys or unnecessary personal data.
- Ownership failures return `404` to reduce resource-existence disclosure.
- Administrator capability is limited to currently implemented product mutations in this Epic.

## Observability requirements

- Record security decision outcomes with correlation context, without token contents or unnecessary personal data.
- Distinguish authentication failures, role failures and ownership failures in internal diagnostics while preserving the external error contract.
- Monitor repeated authentication failures and authorization denials when the observability issue is implemented.
- Do not log raw JWTs, authorization headers or Tool arguments containing private data.

## Global risks and mitigation

- Principal claim mismatch: validate the configured `sub` and reject malformed identity values without falling back to client input.
- Role spoofing: trust only claims from a verified token; never accept a request-body role.
- Ownership bypass through URL parameters: derive the owner from the authenticated context and use owner-scoped queries.
- Tool bypass: test Tools through application services and prohibit repository injection into Tools.
- Error-contract drift: block final implementation until the error contract is approved.
- Missing integration coverage: add REST/security integration tests before closing the Epic.

## ADR index

- [ADR-001 — JWT subject as numeric user identity](adr/ADR-001-jwt-subject.md)
- [ADR-002 — Ownership enforced in application use cases](adr/ADR-002-ownership-application-boundary.md)
- [ADR-003 — Hide private resource existence with 404](adr/ADR-003-private-resource-404.md)
- [ADR-004 — Administrator role from verified JWT claim](adr/ADR-004-admin-role-claim.md)

## Open Questions

Nenhuma. Todas as decisões necessárias para o design de alto nível foram aprovadas na especificação.

## Resolved Questions

- Q-001 a Q-006 permanecem resolvidas conforme `01-spec.md`.
