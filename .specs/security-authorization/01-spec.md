# Product Intent Specification — Authentication and Authorization

## Source

- Tracker: GitHub
- ID: #2 — `[SLDD-01] Specify authentication and authorization`
- URL: https://github.com/Ivictors/ai-ecommerce-assistant/issues/2
- Snapshot date: 2026-09-23

## Goal

Ensure that only authenticated and authorized users can access protected data and operations, preserve isolation between customers' private data, and prevent the frontend, LLM, or Tools from bypassing backend permissions.

## Target Users

- Unauthenticated visitor.
- Authenticated customer.
- Authenticated administrator.
- Internal systems processing authenticated requests on behalf of a user.

## Success Metrics

- Every protected endpoint has an explicit authentication and authorization rule.
- No authenticated customer can read or change private data belonging to another customer.
- No regular customer can execute administrative operations.
- Anonymous requests to protected resources are rejected.
- Requests with invalid or expired credentials are rejected.
- Authorization rules have automated tests for each role and ownership boundary.

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
- Creating customer registration or login.
- Designing password storage or password recovery.
- Implementing OAuth, social login, or multi-factor authentication.
- Implementing payment, refund, return, inventory, or promotion workflows.
- Defining the complete API error payload; this belongs to the error-contract work.
- Implementing authorization before this specification and its review are approved.

## Non-Functional Requirements

No quantitative non-functional requirements are approved for this milestone.

## Security and Privacy Constraints

- Authorization decisions must be deterministic for the same identity, resource, action, and resource state; tests will verify this.
- Security failures must not expose tokens, credentials, private keys, or unnecessary personal data in responses or logs; tests and security review will verify this.
- Authorization must be testable without an LLM call; automated tests will verify this.
- Protected operations must not rely exclusively on client-side validation; backend tests and security review will verify this.

## Glossary

- **Authentication** — Verification that a request is associated with a valid authenticated identity.
- **Authorization** — Decision about whether an authenticated identity may perform an action on a resource.
- **Visitor** — Request without a valid authenticated identity.
- **Customer** — Authenticated user who accesses their own e-commerce data and permitted operations.
- **Administrator** — Authenticated identity with explicitly approved administrative capabilities.
- **Ownership** — Relationship between a private resource and the customer allowed to access it.
- **Protected resource** — Endpoint or business operation requiring authentication, a role, or ownership.
- **Principal** — Identity represented by the authentication credential for the current request.
- **JWT** — Signed token format used by the current technical setup to carry authentication claims.
- **AI Tool** — Callable adapter used by the AI Service to request an application capability.

## Risks and Assumptions

- The current `CurrentUserService` converts the principal name directly to `Long`; this may not match the approved identity claim.
- The current conversation endpoint accepts a `userId` path parameter, which creates an ownership risk if trusted.
- Current product operations do not yet express an administrator boundary.
- Payment and return resources are not fully implemented, so their authorization contract must remain applicable when introduced.
- JWT verification configuration is environment-dependent and must not introduce secrets.
- A role alone may be insufficient for ownership; resource ownership must be checked separately.

## Authorization Matrix — First Security Milestone

| Resource | Access |
|---|---|
| `/api/security/public` | Public |
| `/api/security/private` | Authenticated user |
| `/api/orders` | Authenticated user and own resources only |
| `/api/chat` | Authenticated user and own conversation only |
| `/api/conversations` | Authenticated user and own conversations only |
| `GET /api/products` | Public |
| `POST /api/products` | `role=ADMIN` |
| `DELETE /api/products/{id}` | `role=ADMIN` |

Inventory, promotions, returns, payments, and reporting endpoints are outside this milestone and require their own specifications.

## Open Questions

None.

## Resolved Questions

- **Q-001:** Which JWT claim is the authoritative identifier of the persisted `User`?
  - Resolution: “idnumerico.”
  - Interpretation: The identifier is numeric and comes from the JWT `sub` claim.
  - Resolved at: 2026-09-23
- **Q-002:** What exact role/claim identifies an administrator?
  - Resolution: “role ADMIN”
  - Interpretation: The administrator is identified by the JWT `role` claim with value `ADMIN`.
  - Resolved at: 2026-09-23
- **Q-003:** Which endpoints are public, customer-protected, or administrator-protected in the first security milestone?
  - Resolution: “Suggest” and later “matrix approved.”
  - Interpretation: The authorization matrix in this specification is approved.
  - Resolved at: 2026-09-23
- **Q-004:** Should `ConversationResource` continue accepting `userId` in the URL, or should it list conversations only for the authenticated user?
  - Resolution: “use exclusively the authenticated user.”
  - Interpretation: Conversations use only the authenticated user; client-provided `userId` is not an ownership authority.
  - Resolved at: 2026-09-23
- **Q-005:** Which administrative capabilities are included in the first authorization milestone?
  - Resolution: “protect only currently implemented operations, especially product creation and deletion, leaving inventory, promotions, returns, and reporting to their own issues.”
  - Interpretation: The first milestone protects only currently implemented operations, especially product creation and deletion.
  - Resolved at: 2026-09-23
- **Q-006:** What response semantics should be used when a resource exists but belongs to another customer?
  - Resolution: “not found.”
  - Interpretation: The backend returns `404 Not Found` to avoid revealing another customer's private resources.
  - Resolved at: 2026-09-23
