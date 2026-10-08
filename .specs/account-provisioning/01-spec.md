# Product Intent Specification — MVP Account Provisioning

## Source

- Tracker: GitHub
- ID: #44 — `[SLDD] Securely provision MVP user credentials`
- URL: https://github.com/Ivictors/ai-ecommerce-assistant/issues/44
- Snapshot date: 2026-10-07
- Approval source: user messages on 2026-10-07 approving an operator-controlled
  first-admin bootstrap, removal of test users when the application is ready,
  and an optional test `USER` account created by that bootstrap.

## Goal

Allow the project operator to establish the first application administrator
and, when explicitly requested, a test customer account without exposing
account provisioning through a public unauthenticated API. Ensure every
persisted application account has a non-null Argon2id password hash while
preserving the existing application login and JWT architecture.

## Target Users

- Project operator establishing the first administrator in a local or
  controlled deployment.
- Administrator account used to validate protected MVP operations.
- Optional test customer account used to validate the authenticated customer
  journey.

## Success Metrics

- A fresh installation can be migrated and started without a pre-existing
  administrator or a public bootstrap endpoint.
- The operator can establish the first `ADMIN` through an explicit
  operator-controlled workflow.
- The same bootstrap operation can optionally establish a test `USER` account.
- Both accounts can authenticate through the existing `POST /api/auth/login`
  flow.
- No persisted application user has a null password hash after the approved
  data cleanup and schema transition.
- Existing login, JWT issuance, and server-side role enforcement remain in
  place; no full Keycloak identity-provider integration is introduced.

## Acceptance Criteria (EARS-lite)

- AC-001: When the operator runs the approved bootstrap workflow on an
  installation with no existing administrator, the system shall create the
  first application `ADMIN` account.
- AC-002: Where the operator explicitly requests a test customer during the
  bootstrap workflow, the system shall also create one `USER` account.
- AC-003: When either account is created, the system shall store its password
  only as an Argon2id hash.
- AC-004: If an unauthenticated remote caller attempts to provision an account
  through the application API, then the system shall make no account changes.
- AC-005: If the bootstrap workflow is run when an `ADMIN` already exists,
  then the system shall not create another bootstrap administrator.
- AC-006: When the provisioned `ADMIN` or optional test `USER` submits valid
  credentials to the existing login endpoint, the system shall authenticate
  the account through the existing application login and JWT flow.
- AC-007: When the approved database transition is applied, the system shall
  remove the designated test users and their associated test orders, order
  items, and conversations, and shall require a non-null password hash for
  persisted users.
- AC-008: When the bootstrap workflow handles passwords, the system shall not
  persist or log plaintext passwords, and shall not display them during
  interactive entry.
- AC-009: When the application authorizes a protected operation, the system
  shall continue to enforce roles from the persisted application identity and
  verified application JWT.
- AC-010: If bootstrap account details are incomplete or an email address is
  already assigned to an account, then the system shall create no account with
  those details and shall report a safe failure without disclosing passwords
  or password hashes.
- AC-011: When a bootstrap attempt succeeds or fails, the system shall record
  the outcome without recording passwords, password hashes, or other bootstrap
  secrets.

## Non-Goals

- Replacing application authentication or JWT issuance with Keycloak.
- Creating a public unauthenticated account-provisioning endpoint.
- Self-service customer registration.
- Password recovery or refresh-token lifecycle; these remain tracked
  separately by #43 and #42.
- A reusable post-bootstrap customer-invitation or password-reset workflow.
- Guest checkout, cart, checkout, payment, or order creation.
- Preserving seeded users and their associated seeded orders/conversations;
  these are test data and are to be removed before the application is ready.
- Modifying already-applied Flyway migrations V3 or V6.

## Non-Functional Requirements

No quantitative performance or availability target is approved for this
operator-only MVP workflow. Security and migration constraints are represented
as acceptance criteria and existing repository guardrails.

## Glossary

- **Application account** — A persisted `User` that authenticates against the
  application's own login endpoint.
- **Bootstrap workflow** — An explicit operator-controlled operation used to
  establish the first application administrator and optionally a test user.
- **Test user** — A seeded account and related sample data used only for
  development/validation, not a real customer identity.
- **Argon2id hash** — The one-way password representation already approved for
  application credential storage.
- **Keycloak bootstrap pattern** — A temporary, controlled setup mechanism;
  this project uses the operational pattern only and does not delegate
  application authentication to Keycloak.

## Risks and Assumptions

- Flyway currently runs at application startup; the first-admin workflow must
  therefore be usable after migrations complete and must not depend on an
  already-protected ADMIN API.
- V3 seeds three users and associated orders, order items, and conversations;
  a forward migration must identify and remove only those designated test
  records in foreign-key-safe order.
- V6 adds nullable `password_hash`; a later forward migration must enforce the
  approved non-null invariant only after designated passwordless test users
  are removed.
- This is an MVP provisioning path, not a complete production account
  lifecycle. A reusable customer onboarding and password recovery design is
  deferred.

## Open Questions

None at product-intent level. Technical design must document the transaction
and failure behavior when optional test-user creation is requested, without
changing the approved account outcomes.

## Resolved Questions

- **Q-001:** How must the operator provide bootstrap passwords?
  - Resolution: “sim aprovado” in response to the recommendation to use an
    interactive prompt that does not display password characters. The
    bootstrap workflow uses hidden interactive password entry; command-line
    arguments and environment variables are not the approved input method.
  - Resolved at: 2026-10-07
- **Q-002:** Should initial application authentication be migrated to
  Keycloak, or should only its controlled bootstrap pattern inform the
  first-admin setup?
  - Resolution: “podemos usar o keycloack conforme sua recomendação sem mudar
    toda a estrutura de login. apenas para admin provisorio.” The project keeps
    its current login/JWT architecture and uses an operator-controlled
    bootstrap pattern only; it does not integrate Keycloak as the application's
    identity provider in this MVP.
  - Resolved at: 2026-10-07
- **Q-003:** How should existing passwordless seeded users be handled?
  - Resolution: “em relação aos usuarios ja existentes, são apenas para teste
    quando a applicação estiver pronta, apagaremos eles.” The designated test
    users and their associated test data are removed before the application is
    ready; real user data must not be deleted by a broad predicate.
  - Resolved at: 2026-10-07
- **Q-004:** Should the operator bootstrap also support a test customer?
  - Resolution: “a opção 1 é a mais correta.” The approved option is one
    operator-controlled bootstrap workflow that creates the first `ADMIN` and
    optionally a test `USER` when explicitly requested.
  - Resolved at: 2026-10-07
- **Q-005:** Must persisted application users have a non-null password hash?
  - Resolution: “senha não pode ser null, devemos corrigir.” The final
    application-user schema requires a non-null password hash; existing
    passwordless test data will be removed through forward migrations.
  - Resolved at: 2026-10-07
