# Especificação de Intenção — Autenticação e Autorização

## Source

- Tracker: GitHub
- ID: #2 — `[SLDD-01] Especificar autenticação e autorização`
- URL: https://github.com/Ivictors/ai-ecommerce-assistant/issues/2
- Snapshot date: 2026-09-23

## Goal

Garantir que somente usuários autenticados e autorizados acessem dados e operações protegidos, preservando o isolamento dos dados privados de cada cliente e impedindo que frontend, LLM ou Tools ultrapassem as permissões definidas pelo backend.

## Target Users

- Visitante não autenticado.
- Cliente autenticado.
- Administrador autenticado.
- Sistemas internos que processam chamadas autenticadas em nome de um usuário.

## Success Metrics

- Todos os endpoints protegidos possuem uma regra explícita de autenticação e autorização.
- Nenhum cliente autenticado consegue consultar ou alterar dados privados pertencentes a outro cliente.
- Nenhum cliente comum consegue executar operações administrativas.
- Chamadas anônimas a recursos protegidos são rejeitadas.
- Chamadas com credenciais inválidas ou expiradas são rejeitadas.
- As regras de autorização possuem testes automatizados para cada perfil e limite de ownership.

## Acceptance Criteria (EARS-lite)

- AC-001: The system shall distinguish requests from unauthenticated visitors, authenticated customers, and authenticated administrators.
- AC-002: When an unauthenticated visitor accesses a protected resource, the system shall reject the request as unauthenticated.
- AC-003: When a request contains an invalid or expired authentication credential, the system shall reject the request as unauthenticated.
- AC-004: When an authenticated customer accesses private customer data, the system shall allow access only to data owned by that customer.
- AC-005: If an authenticated customer attempts to access another customer's private data, then the system shall reject the request.
- AC-006: When an authenticated customer accesses an order, the system shall evaluate ownership using the authenticated identity rather than a client-provided owner identifier.
- AC-007: When an authenticated customer accesses a conversation, the system shall evaluate ownership using the authenticated identity rather than a client-provided owner identifier.
- AC-008: When an authenticated customer accesses payment or return information, the system shall evaluate ownership using the authenticated identity.
- AC-009: When an authenticated customer invokes an administrative operation, the system shall reject the request.
- AC-010: When an authenticated administrator invokes an operation approved for administrators, the system shall authorize the operation.
- AC-011: The system shall enforce authorization at the backend boundary independently of frontend route protection.
- AC-012: The system shall enforce authorization for AI Tools through application use cases before any protected business operation is executed.
- AC-013: If an LLM provides an argument that conflicts with the authenticated user's permissions, then the system shall reject the protected operation.
- AC-014: The system shall not treat frontend-provided ownership, role, price, stock, payment, or authorization values as authoritative.
- AC-015: When authentication or authorization rejects a request, the system shall return the error representation defined by the approved API error contract.

## Non-Goals

- Implementing or replacing the JWT provider.
- Creating a customer registration or login flow.
- Designing password storage or password recovery.
- Implementing OAuth, social login, or multi-factor authentication.
- Implementing payment, refund, return, inventory, or promotion workflows.
- Defining the complete API error payload; this belongs to the error-contract work, while this specification only requires its use.
- Implementing authorization changes before this specification and its review are approved.

## Non-Functional Requirements

- Authorization decisions shall be deterministic for the same authenticated identity, resource, action, and current resource state.
- Security failures shall not expose tokens, credentials, private keys, or unnecessary personal data in responses or logs.
- Authorization logic shall be testable without requiring an LLM call.
- Protected business operations shall not rely exclusively on client-side validation.

## Glossary

- **Authentication** — Verification that a request is associated with a valid authenticated identity.
- **Authorization** — Decision about whether an authenticated identity may perform a specific action on a specific resource.
- **Visitor** — Request without a valid authenticated identity.
- **Customer** — Authenticated user who accesses their own e-commerce data and permitted customer operations.
- **Administrator** — Authenticated identity with explicitly approved administrative capabilities.
- **Ownership** — Relationship between a private resource and the customer who is allowed to access it.
- **Protected resource** — Endpoint or business operation that requires authentication and/or a specific role or ownership check.
- **Principal** — Identity represented by the authentication credential for the current request.
- **JWT** — Signed token format used by the current technical setup to carry authentication claims.
- **AI Tool** — Callable adapter used by the AI Service to request an application capability.

## Risks and Assumptions

- The current `CurrentUserService` converts the principal name directly to `Long`; this may not match the approved identity claim.
- The current conversation endpoint accepts a `userId` path parameter, which can create an ownership risk if it is trusted.
- The current product operations do not yet express an administrator boundary.
- Payment and return resources are not fully implemented, so their authorization contract must remain applicable when those use cases are introduced.
- JWT verification configuration is environment-dependent and must not introduce secrets into the repository.
- A role name alone may be insufficient for ownership; resource ownership must be checked separately.

## Open Questions

- Nenhuma.

## Resolved Questions

- **Q-001:** Which JWT claim is the authoritative identifier of the persisted `User`?
  - Resolution: O identificador será numérico e virá do claim `sub` do JWT.
  - Resolved at: 2026-09-23

- **Q-002:** What exact role/claim identifies an administrator?
  - Resolution: O administrador será identificado pelo claim `role` com o valor `ADMIN`.
  - Resolved at: 2026-09-23

- **Q-003:** Which endpoints are public, customer-protected, or administrator-protected in the first security milestone?
  - Resolution: A matriz inicial recomendada é: `/api/security/public` público; `/api/security/private` protegido para usuário autenticado; `/api/orders` protegido por ownership do cliente; `/api/chat` protegido por usuário autenticado e conversa pertencente ao cliente; `/api/conversations` protegido por ownership do cliente; `GET /api/products` público; `POST /api/products` e `DELETE /api/products/{id}` protegidos para `ADMIN`. Endpoints futuros de estoque, promoções, retornos, pagamentos e relatórios ficam fora desta milestone.
  - Resolved at: 2026-09-23

- **Q-004:** Should `ConversationResource` continue accepting `userId` in the URL, or should it list conversations only for the authenticated user?
  - Resolution: As conversas usarão exclusivamente o usuário autenticado; o `userId` fornecido pelo cliente não será usado como autoridade de ownership.
  - Resolved at: 2026-09-23

- **Q-005:** Which administrative capabilities are included in the first authorization milestone?
  - Resolution: A primeira milestone protegerá somente as operações atualmente implementadas, principalmente criação e remoção de produtos. Estoque, promoções, retornos e relatórios terão issues próprias.
  - Resolved at: 2026-09-23

- **Q-006:** What response semantics should be used when a resource exists but belongs to another customer?
  - Resolution: O backend retornará `404 Not Found` para não revelar a existência de recursos privados de outro cliente.
  - Resolved at: 2026-09-23
