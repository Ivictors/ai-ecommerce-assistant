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

## Traceability status

This file intentionally covers the first Epic slice only. AC-004 through AC-015 are mapped to later slices in `03a-epic-roadmap.md` and will receive tasks before their implementation begins.
