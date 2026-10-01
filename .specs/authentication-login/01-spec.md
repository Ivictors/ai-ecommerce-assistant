# Product Intent Specification — Backend Login and JWT Issuance

## Source

- Tracker: GitHub
- ID: #35 — `[SLDD] Implement backend login and JWT issuance`
- URL: https://github.com/Ivictors/ai-ecommerce-assistant/issues/35
- Snapshot date: 2026-09-30

## Goal

Allow a registered user to authenticate with email and password and receive a
short-lived RSA-signed access token that the Angular frontend can store in
`sessionStorage` and use for protected API requests.

## Target Users

- Registered customer.
- Registered administrator.
- Angular frontend consuming the login contract.

## Success Metrics

- Valid credentials produce a token accepted by protected endpoints.
- Invalid credentials produce a uniform authentication failure.
- Passwords are stored only as Argon2id hashes.
- JWT `sub` is the numeric persisted user ID and `role` comes from persisted
  user data.
- Secrets, passwords, hashes, and tokens are not exposed in responses or logs.

## Acceptance Criteria (EARS-lite)

- AC-001: When a registered user submits valid email and password credentials,
  the system shall return a Bearer access token response.
- AC-002: When valid credentials are submitted, the system shall issue a token
  with the numeric persisted user ID as `sub`.
- AC-003: When valid credentials are submitted, the system shall issue a token
  with the persisted user role as `role`.
- AC-004: If the email does not exist or the password is incorrect, then the
  system shall return the same safe authentication failure without revealing
  which credential was invalid.
- AC-005: The system shall store only an Argon2id password hash and shall never
  persist the raw password.
- AC-006: When a protected endpoint receives a valid issued token, the system
  shall apply the existing authentication, role, and ownership rules.
- AC-007: If an issued token is expired, malformed, or signed with an unknown
  key, then the system shall reject it as unauthenticated.
- AC-008: The system shall load RSA signing material from runtime configuration
  and shall not require private keys to be committed to the repository.
- AC-009: The system shall not log passwords, password hashes, private keys, or
  complete access tokens.
- AC-010: The first authentication delivery shall not include refresh tokens or
  password recovery.

## Non-Goals

- Refresh tokens; tracked for future work in issue #42.
- Password recovery; tracked for future work in issue #43.
- User registration, email verification, social login, OAuth, and MFA.
- Password change after authentication.
- Account lockout and advanced fraud detection.

## Non-Functional Requirements

No quantitative performance requirement is approved for the first delivery.
Password hashing must use the approved Argon2id library configuration and
runtime secret material must remain outside source control.

## Glossary

- **Access token** — Short-lived signed JWT used to authenticate API requests.
- **Argon2id** — Memory-hard password hashing algorithm used for password
  verification.
- **Bearer token** — Token presented in the HTTP `Authorization` header.
- **Role** — Persisted authorization classification such as `USER` or `ADMIN`.
- **RSA signing material** — Private key used to sign tokens and public key used
  to verify them.
- **Uniform authentication failure** — Response that does not reveal whether
  the email or password was incorrect.

## Risks and Assumptions

- Existing users have no password hash or persisted role, so a migration and
  approved seed/update strategy are required. Existing users receive role
  `USER` by default, keep `password_hash` null until securely provisioned, and
  cannot authenticate while the hash is null. Secure provisioning is deferred
  to issue #44.
- Existing JWT verification uses a public key; token issuance requires the
  matching private key at runtime.
- Argon2id increases authentication CPU/memory cost intentionally to resist
  password cracking.
- The frontend will use `sessionStorage` for the access token; it will not
  receive or store private signing material.

## Resolved Questions

- **Q-001:** What login identifier is used?
  - Resolution: "login: email + password"
  - Resolved at: 2026-09-30
- **Q-002:** Where does the authorization role come from?
  - Resolution: "role: persistida no usuário"
  - Resolved at: 2026-09-30
- **Q-003:** Which password hash is approved?
  - Resolution: "hash: Argon2id"
  - Resolved at: 2026-09-30
- **Q-004:** Which JWT signing strategy is approved?
  - Resolution: "JWT: RSA"
  - Resolved at: 2026-09-30
- **Q-005:** Are refresh tokens part of the first version?
  - Resolution: "refresh token: fora desta primeira versão"
  - Resolved at: 2026-09-30
- **Q-006:** Is password recovery part of the first version?
  - Resolution: "password recovery: fora desta primeira versão"
  - Resolved at: 2026-09-30
- **Q-007:** How should existing users without credentials be handled?
  - Resolution: "A base atual possui usuários sem senha. Recomendo: adicionar
    password_hash e role; usar USER como role padrão; manter password_hash
    temporariamente nulo; impedir login enquanto o usuário não tiver credencial
    provisionada; criar posteriormente um mecanismo administrativo seguro para
    provisionar senhas."
  - Resolved at: 2026-10-01

## Open Questions

None. Refresh tokens, password recovery, and secure credential provisioning
are explicitly deferred to issues #42, #43, and #44.
