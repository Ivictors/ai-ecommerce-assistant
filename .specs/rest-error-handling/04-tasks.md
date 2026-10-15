# Tasks — Angular REST Error Handling

### T-001

- Add the shared error state and HTTP interceptor.
- Tests: status mapping, code preservation, safe messages, 401 session clearing, and 403 session preservation.
- Acceptance: `npm test -- --watch=false` passes.

### T-002

- Register the interceptor after authentication and validate the production build.
- Acceptance: Angular tests and `npm run build` pass.
