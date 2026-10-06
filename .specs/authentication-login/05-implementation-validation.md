# Implementation and Validation — Backend Login and JWT Issuance

## Implemented

- Added persisted `USER`/`ADMIN` role and Argon2id-backed password credentials
  through Flyway V6; users without a password hash cannot log in.
- Added RSA-signed access-token issuance and `POST /api/auth/login`.
- Configured SmallRye JWT to map the `role` claim to Quarkus security roles.
- Made JWT issuer and verification-key locations configurable and corrected the
  misspelled example configuration filename; moved that template out of
  `src/main/resources` so Quarkus does not treat it as runtime configuration.
- Added end-to-end tests using ephemeral test-only RSA keys and isolated test
  users; no production credential or signing key is needed by the tests.

## Validation evidence

Run on 2026-10-05 against the repository PostgreSQL Compose service:

- `mvnw.cmd clean verify` — passed (exit code 0).
- Flyway validated all six migrations; schema version 6 was current.
- Quarkus started with explicit Hibernate `schema-management.strategy=validate`
  and reported no schema mismatch; Flyway remains responsible for schema
  changes.
- The local database contained no knowledge documents, versions, or chunks, so
  there were no pre-existing knowledge rows available for a data round-trip.
  No data migration was needed or performed.
- Four authentication integration scenarios passed:
  - valid login token is accepted by an authenticated endpoint;
  - a `USER` token is denied (`403`) by product creation while an `ADMIN`
    token is accepted (`201`);
  - wrong password, unknown email, and a user with no password hash receive
    the same safe authentication failure;
  - expired and unknown-signature JWTs are rejected (`401`).
- The full Maven test report contains no failing or errored tests.
- `git diff --check` passed. Test signing keys are generated in a temporary
  directory and removed after the integration-test class.

## Traceability

| Acceptance behavior | Evidence |
| --- | --- |
| Login contract and uniform invalid-credential result | `AuthenticationResourceTest`, `AuthenticationIntegrationTest` |
| Persisted subject/role and signed token | `RsaAccessTokenIssuerTest`, `AuthenticationIntegrationTest` |
| Existing protected endpoints accept issued tokens | `AuthenticationIntegrationTest.validUserLoginMustWorkOnProtectedEndpoint` |
| Server-side role enforcement | `AuthenticationIntegrationTest.persistedRoleMustControlAdminEndpointAccess` |
| Expiration and signature verification | `AuthenticationIntegrationTest.expiredOrUnknownSignatureTokensMustBeRejected` |
| No production secrets required for tests | `AuthenticationIntegrationTestProfile` |

## Review status

Implementation and validation are complete; the changes are intentionally
uncommitted pending the developer's review. Issue #35 remains open until the
reviewed changes are committed. Issues #42, #43, and #44 remain separately
tracked future work.
