# Validation Report

- `npm test -- --watch=false`: PASS — 38 tests in 14 files.
- `npm run build`: PASS.
- Orders and conversations use authenticated backend endpoints; no owner ID is accepted from the UI as authority.
- Conversation selection requires a positive backend-issued numeric ID.
