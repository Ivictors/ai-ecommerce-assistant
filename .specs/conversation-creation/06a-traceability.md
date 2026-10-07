# Traceability — Authenticated Conversation Creation

| AC | Task | Test evidence | Implementation | Gate |
| --- | --- | --- | --- | --- |
| AC-001 | T-001 | T-001-T1 `ConversationServiceTest`; T-001-T2 resource and integration tests | `ConversationService.createForUser`; `ConversationResource.create`; `ConversationResponse.from` | PASS |
| AC-002 | T-001 | T-001-T6 `ConversationCreationIntegrationTest` | `ConversationRepository.findByUserId`; `ConversationResource.findCurrentUserConversations` | PASS |
| AC-003 | T-001 | T-001-T3/T4/T9 integration tests; T-001-T9 service and mapper tests | `@RolesAllowed("USER")`; `CurrentUserService`; `UnauthenticatedUserExceptionMapper` | PASS |
| AC-004 | T-001 | T-001-T5 `ConversationCreationIntegrationTest` | `ConversationResource.create` accepts no owner parameter; service uses JWT subject | PASS |
| AC-005 | T-001 | T-001-T7 `ConversationCreationIntegrationTest`; existing owner-boundary unit test | `ChatApplicationService.chat`; `ConversationService.findByIdForUser` | PASS |
| AC-006 | T-001 | T-001-T8 `ChatApplicationServiceTest` | `ChatApplicationService.chat`; owner-scoped lookup | PASS |

All feature tests map to an acceptance criterion and task. No new database
schema or dependency is introduced.
