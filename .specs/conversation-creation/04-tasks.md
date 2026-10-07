# Tasks — Authenticated Conversation Creation

### T-001: Create and expose an owned conversation

- AC-IDs: AC-001, AC-002, AC-003, AC-004, AC-005, AC-006
- Test-IDs:
  - T-001-T1 — application service loads owner and persists conversation
  - T-001-T2 — REST create returns 201 and existing response metadata
  - T-001-T3 — unauthenticated request returns 401 without persistence
  - T-001-T4 — missing USER role returns 403 without persistence
  - T-001-T5 — client owner spoof attempt cannot alter persisted owner
  - T-001-T6 — created conversation appears in creator list
  - T-001-T7 — other-user list/chat lookup cannot access created row
  - T-001-T8 — creator ID resolves through owner-scoped chat lookup
  - T-001-T9 — missing persisted caller row is rejected without persistence
- Files in scope:
  - `src/main/java/com/victor/ecommerce/application/conversation/ConversationService.java`
  - `src/main/java/com/victor/ecommerce/presentation/rest/conversation/ConversationResource.java`
  - `src/test/java/com/victor/ecommerce/application/conversation/ConversationServiceTest.java`
  - `src/test/java/com/victor/ecommerce/presentation/rest/conversation/ConversationResourceTest.java`
  - `src/test/java/com/victor/ecommerce/presentation/rest/conversation/ConversationCreationIntegrationTest.java`
  - `src/test/java/com/victor/ecommerce/presentation/rest/conversation/ConversationResourceTest.java`
  - `src/test/java/com/victor/ecommerce/application/conversation/ConversationServiceTest.java`
  - `src/test/java/com/victor/ecommerce/application/chat/ChatApplicationServiceTest.java`
  - `src/main/java/com/victor/ecommerce/presentation/rest/error/UnauthenticatedUserExceptionMapper.java`
  - test profile/helper for temporary RSA keys if required
- Dependencies: none; #35 is complete.
- Gates: unit, REST contract, PostgreSQL integration, authorization/ownership,
  full Maven verification, secret review.
- Rollback: revert the single feature commit; no database migration.
- Notes: Keep one task/commit for this bounded endpoint; do not add chat
  history or conversation management features.

## Parallel Work

No implementation tasks are independent within this slice. The client work
tracked by #39/#40 may use this stable API contract after T-001 is complete.
