# Low-Level Design — S-001 Authenticated Identity

Feature: authentication and authorization
Slice: S-001 — Authenticated identity
Source spec: `01-spec.md`
High-level design: `03-epic-design.md`

## API contracts

S-001 does not add a new public endpoint. It defines the authenticated identity consumed by protected application use cases.

| Input | Source | Rule |
|---|---|---|
| Authentication state | Quarkus `SecurityIdentity` | Anonymous identities are rejected by protected use cases. |
| User identifier | verified JWT `sub` exposed as principal name | Must represent a valid numeric `User` identifier. |

The final HTTP status and error payload are owned by the error-contract work in issue #5. S-001 must expose a typed application/security failure that can be mapped by that contract; it must not expose stack traces or token contents.

## Data models

- `SecurityIdentity`: Quarkus-provided request identity and authentication state.
- `Principal`: verified identity name, expected to contain the numeric JWT `sub`.
- `CurrentUserService`: application adapter that returns the current numeric user ID or raises a security failure.
- `User`: existing persisted customer entity identified by `Long id`.

No database migration is required for S-001. The persisted `User` schema remains unchanged.

## Error model

| Condition | Internal behavior | External mapping |
|---|---|---|
| Anonymous identity | Reject current-user resolution | 401 through approved error contract |
| Missing principal | Reject current-user resolution | 401 through approved error contract |
| Non-numeric `sub` | Reject current-user resolution | 401 through approved error contract |
| Valid numeric `sub` | Return `Long` user ID | Continue application flow |

The implementation must not silently fall back to a request parameter, request body, email, or other client-provided value.

## Security details

- Read identity only from the verified `SecurityIdentity`.
- Treat the principal name as untrusted until it is validated as a numeric identifier.
- Do not log the JWT, authorization header, or raw principal value on failure.
- Keep identity resolution independent from LLM calls and repository policy.
- Resource ownership and administrator role checks are subsequent slices; S-001 only establishes the current identity.

## Test strategy

- Unit-test `CurrentUserService` with a mocked `SecurityIdentity`.
- Verify anonymous identity rejection.
- Verify missing principal rejection.
- Verify malformed/non-numeric principal rejection.
- Verify valid numeric principal conversion.
- Verify no fallback to client-provided values exists in the service contract.

No REST or database integration test is required for S-001 because it does not change an endpoint or schema. Those tests belong to S-002 through S-005.

## Test scenario catalog with edge cases

| Test ID | Scenario | Expected result |
|---|---|---|
| S001-T1 | `SecurityIdentity.isAnonymous()` is true | Identity resolution fails as unauthenticated. |
| S001-T2 | Identity is authenticated but principal is absent | Identity resolution fails as unauthenticated. |
| S001-T3 | Principal name is non-numeric | Identity resolution fails as unauthenticated. |
| S001-T4 | Principal name is empty or whitespace | Identity resolution fails as unauthenticated. |
| S001-T5 | Principal name is a valid positive numeric ID | Returns the corresponding `Long`. |
| S001-T6 | Principal name contains a signed numeric value | Convert it as a numeric subject; resource existence and ownership are handled by later slices. |
| S001-T7 | Principal name exceeds `Long` range | Identity resolution fails without leaking token data. |

## Dependency/version policy

- Java runtime: Java 21, matching the project compiler release.
- Quarkus: 3.39.3, kept aligned across the imported Quarkus BOMs.
- Security API: `io.quarkus:quarkus-smallrye-jwt` from the existing Quarkus platform.
- Test framework: existing Quarkus JUnit 5 and Mockito dependencies.
- No new dependency is required for S-001.
- Do not upgrade framework or runtime versions as part of this slice.

## Traceability

| Acceptance criterion | S-001 coverage |
|---|---|
| AC-001 | S001-T1, S001-T5; role distinction is completed in S-002. |
| AC-002 | S001-T1; endpoint-level enforcement is completed in S-002. |
| AC-003 | S001-T2 through S001-T7; JWT verification and REST mapping are completed in S-005. |

## Open Questions

None.
