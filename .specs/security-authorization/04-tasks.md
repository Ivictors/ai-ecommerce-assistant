# Tasks — S-001 Authenticated Identity

### T-001: Define the current-user security failure contract

- AC-IDs: AC-001, AC-002, AC-003
- Test-IDs: S001-T1, S001-T2, S001-T3, S001-T4, S001-T6, S001-T7
- Files in scope: `backend/ai-ecommerce-assistant/src/main/java/com/victor/ecommerce/application/security/CurrentUserService.java`, security exception type, matching unit test
- Dependencies: none
- Gates: unit
- Rollback: revert commit; no schema change
- Notes: The failure must be safe for later mapping to the approved 401 error contract. Do not implement endpoint changes in this task.

### T-002: Resolve valid numeric JWT subject

- AC-IDs: AC-001, AC-003
- Test-IDs: S001-T5
- Files in scope: `CurrentUserService.java`, matching unit test
- Dependencies: T-001
- Gates: unit
- Rollback: revert commit; no schema change
- Notes: The verified principal name is the only source for the current numeric user ID.

## Slice gate

- S-001 is complete when T-001 and T-002 are done and all S001 tests pass.
- Endpoint, role, ownership, Tool, and error-contract work remains in later slices.

## Parallel work

T-001 and T-002 are sequential because T-002 depends on the failure contract from T-001.

## S-002 — Endpoint and administrator matrix

### T-003: Protect customer endpoints with the USER role

- AC-IDs: AC-001, AC-002, AC-004, AC-011
- Test-IDs: S002-T1, S002-T2, S002-T3
- Files in scope: chat, conversation, and order REST resources; matching security tests
- Dependencies: T-001, T-002
- Gates: unit, REST
- Rollback: revert commit; no schema change
- Notes: Ownership filtering remains a later task; this task establishes the endpoint authentication boundary.

### T-004: Protect product mutations with the ADMIN role

- AC-IDs: AC-009, AC-010, AC-011, AC-014
- Test-IDs: S002-T4, S002-T5
- Files in scope: `ProductResource.java`; matching security tests
- Dependencies: T-003
- Gates: unit, REST
- Rollback: revert commit; no schema change
- Notes: Product reads remain public; only currently implemented mutations are administrative.

## S-003 — Order and conversation ownership

### T-005: Enforce conversation ownership in application queries

- AC-IDs: AC-004, AC-005, AC-007
- Test-IDs: S003-T1, S003-T2
- Files in scope: `ConversationRepository.java`, `ConversationService.java`, `ConversationResource.java`, matching tests
- Dependencies: T-003
- Gates: unit, REST
- Rollback: revert commit; no schema change
- Notes: The authenticated user is the only ownership source; client `userId` is removed from the conversation listing contract.

### T-006: Enforce conversation ownership before AI chat memory access

- AC-IDs: AC-005, AC-007, AC-012, AC-013
- Test-IDs: S003-T3, S003-T4
- Files in scope: `ChatApplicationService.java`, matching tests
- Dependencies: T-005
- Gates: unit
- Rollback: revert commit; no schema change
- Notes: A conversation not owned by the authenticated user is treated as not found and must not invoke the AI service.

## Traceability status

This file intentionally covers the first Epic slice only. AC-004 through AC-015 are mapped to later slices in `03a-epic-roadmap.md` and will receive tasks before their implementation begins.
