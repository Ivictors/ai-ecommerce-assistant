# Known Technical Debt and Follow-up Work

This file distinguishes completed MVP work from remaining engineering debt.
Open items are tracked by GitHub issue and must not be implemented by
inference from this checklist alone.

## Completed in the MVP

- Authentication, authorization, JWT subject resolution, and ownership
  enforcement — issues #35, #36, #39, #40, #44, and #47.
- Standardized REST errors — issue #37.
- Conversation creation and persistence — issue #47.
- RAG document lifecycle, processing, scoped retrieval, and integration —
  issues #19-#25.
- Angular MVP screens and structured chat rendering — issues #36-#41.
- Observability, containers, LTS alignment, and hardening review — issues #13,
  #14, #15, and #48.
- `source-map-js` audit finding — commit `e446bb2`.

## Current MVP closure work

- [ ] Reconcile remaining repository documentation — issue #49.
- [ ] Execute browser and live AI/RAG acceptance — issue #50.

## Quality and operational debt

- [ ] Add CI quality gates — issue #51.
- [ ] Separate and strengthen unit/integration test execution and reassess the
  current Failsafe skip — issue #52.
- [ ] Establish static analysis, coverage, formatting, and test-runtime hygiene
  — issue #53.
- [ ] Define production hardening, secrets management, backups, TLS, and
  rollback — issue #54.

## Post-MVP product and AI evolution

- [ ] Implement refresh-token lifecycle — issue #42.
- [ ] Implement password recovery — issue #43.
- [ ] Establish repeatable RAG quality and cost evaluation — issue #55.
- [ ] Define transactional commerce workflows — issue #56.

## Explicitly deferred

Checkout, payments, advanced inventory, promotions, returns, reports, external
document URLs, user-owned documents, asynchronous document jobs, centralized
log analysis, and production deployment are not part of the current MVP.
