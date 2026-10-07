# Low-Level Design — Authenticated Conversation Creation

## API Contracts

### `POST /api/conversations`

- Request body: absent; no owner ID is accepted as an input field.
- Authentication: JWT required; only `USER` role is authorized.
- Success: `201 Created` with existing `ConversationResponse` JSON fields:
  `id`, `userId`, `createdAt`.
- `401`: existing `UNAUTHENTICATED` error for anonymous/invalid identity.
- `403`: existing `FORBIDDEN` error for an authenticated caller without the
  required role.
- `500`: existing safe internal-error response for unexpected persistence
  failures.
- No new error code or migration is introduced.

The collection `GET /api/conversations` and `POST /api/chat` contracts remain
unchanged. No `Location` header is added because a resource-specific GET route
does not exist.

## Data Models

- Domain: existing `Conversation(User)` constructor sets owner and timestamp.
- Persistence: existing `conversations` table uses generated `id`, non-null
  `user_id`, and non-null `created_at`.
- Existing `ConversationRepository` persists the new entity; existing
  `UserRepository` resolves the caller subject.
- The create use case is transactional; no schema migration or library change
  is required.

## Error Model

| Condition | HTTP result | AC |
| --- | --- | --- |
| No valid caller identity | `401 UNAUTHENTICATED` | AC-003 |
| Caller has no `USER` role | `403 FORBIDDEN` | AC-003 |
| Owner ID appears in query/body | Never used to choose owner; persisted owner remains JWT subject | AC-004 |
| Other user accesses chat with ID | Existing `404` conversation-not-found behavior | AC-005 |
| Persistence fails unexpectedly | Existing generic `500` mapping; no internals disclosed | NFR/security |

## Security Details

- Resource authorization uses the existing class-level `@RolesAllowed("USER")`.
- The resource obtains the caller ID using `CurrentUserService`; no request
  parameter or body DTO contains an owner ID.
- `ConversationService` receives the trusted ID explicitly and persists the
  conversation for the matching database user.
- Tests use synthetic users and test-only RSA key material; never use
  production keys or call a production identity provider.
- Ownership is verified at application/repository boundaries, consistent with
  accepted ADR-002.

## Test Strategy

- Unit-test `ConversationService` to prove it loads the specified persisted
  user, creates an associated domain object, and persists it.
- Resource test proves status 201, response body mapping, and delegation with
  the current authenticated ID.
- Quarkus integration test uses isolated database users and a temporary RSA
  key pair to verify authentication, ownership, and list visibility.
- Cross-owner chat test expects 404 before assistant invocation; no live LLM
  or OpenAI credential is needed.
- Full backend gate: `mvnw.cmd clean verify` with Docker Compose PostgreSQL.

## Test Scenario Catalog

- T-001-T1: user exists; application service creates and persists conversation
  associated with that user (AC-001).
- T-001-T2: authenticated USER POST receives `201`, generated ID, correct
  `userId`, and `createdAt` (AC-001).
- T-001-T3: no bearer token returns `401` and row count is unchanged (AC-003).
- T-001-T4: valid token with non-USER role returns `403` and row count is
  unchanged (AC-003).
- T-001-T5: owner-selection query/body attempt cannot change the persisted
  owner from the token subject (AC-004).
- T-001-T6: created row appears in creator's `GET /api/conversations` result
  (AC-002).
- T-001-T7: created row is absent from another user's list and cross-user chat
  lookup returns 404 without invoking the assistant (AC-005).
- T-001-T8: creator ID resolves through the existing owner-scoped chat lookup
  used by chat (AC-006).
- T-001-T9: if the validated subject has no persisted user row, creation is
  rejected with the existing 401 response and no conversation is persisted.
- No schema, PDF, OpenAI, or network-provider integration test is added.

If a validly signed token refers to a user row that no longer exists, the
application treats the caller as unauthenticated and returns the existing
`401 UNAUTHENTICATED` response. This prevents creation without a persisted
owner.

## Dependency/Version Policy

- Java: retain the repository's Java 21 LTS baseline; no runtime-version
  change is required for this feature.
- Quarkus: retain the existing Quarkus 3.39.3 platform pin for this isolated
  slice; do not mix a platform upgrade into conversation behavior.
- The Quarkus 3.39 maintenance stream reached end of community maintenance on
  2026-09-30; issue #14 must verify the framework baseline and move to a
  supported LTS release before any production deployment ([Quarkus release
  status](https://quarkus.io/releases/)).
- No new dependency is needed; use the existing Quarkus REST, Panache, JWT,
  and JUnit/Mockito stack.
- Existing Maven dependency versions remain controlled by the Quarkus BOM.
- Java 21 is an LTS release with planned Oracle Premier Support through
  September 2028 ([Java SE support roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)).
