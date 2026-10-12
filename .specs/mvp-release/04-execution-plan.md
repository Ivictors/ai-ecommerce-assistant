# MVP Execution Plan and Task Acceptance Gates

Planning date: 2026-10-12. Status: execution authorized by the subsequent
owner instruction "implemente". Documentation language: English.

## Sources and scope

- [Approved MVP intent](01-spec.md), [epic design](03-epic-design.md), and
  [roadmap](03a-epic-roadmap.md).
- [Account provisioning specification](../account-provisioning/01-spec.md),
  [design](../account-provisioning/04-low-level-design.md), and
  [tasks](../account-provisioning/04-tasks.md).
- GitHub issues #44, #36-#41, #12, #13, #14, #48, and #15.
- Owner instruction: execute the reviewed plan and make an individual
  Conventional Commit after each implementation task is finished.

This document defines delivery gates for the remaining MVP. Detailed account
tasks retain their existing T-001 through T-005 identifiers. Later issues are
delivery slices; their file-level T-NNN decomposition and Test-IDs must be
prepared in their own feature directory before implementation, following SLDD
in roadmap order. Do not invent endpoint contracts or product rules to fill
gaps during coding.

## Verified starting point

| Item | State on inspection | Evidence / limit |
| --- | --- | --- |
| #35 login and #47 conversation creation | Closed in GitHub | Existing backend prerequisites; not revalidated during this planning pass. |
| #44 / T-001 | Implemented and committed | `55d23d5` is on local and remote main; migration test results are recorded. |
| #44 / T-002 | Locally implemented, not committed | Previous log records 86 passing backend tests; this is historical evidence, not a new run. |
| #44 / T-003 | Incomplete RED | Existing five failures check missing types; required behavioral tests are not complete. |
| #44 / T-004 and T-005 | Pending | No completed command or end-to-end provisioning delivery. |
| #36-#41 / #12 | Open | Angular session/API foundations exist; complete customer screens and validation are pending. |
| #13, #14, #48, #15 | Open | Operational and final delivery gates remain. |

## Completion and commit policy

1. Work sequentially on `main`. Read the current issue, approved specification,
   existing tests, and worktree before each task.
2. A behavior change requires behavioral RED evidence before its production
   implementation. Class/method existence assertions alone do not establish
   transaction, concurrency, authorization, or logging behavior.
3. Complete GREEN, REFACTOR, and SIMPLIFY; run the relevant regressions. Record
   commands, environment, outcomes, and AC-to-Test-ID traceability. Compilation
   alone is not completion.
4. Review the task diff, secrets, and scope. Stage explicit paths/hunks only.
   Preserve `.env`, private keys, and unrelated local work. Where documents
   contain multiple tasks, stage only the completed task's hunks.
5. Create one focused Conventional Commit per completed T-NNN task; include
   its parent issue number and task ID. If an issue needs several tasks, each
   gets its own commit. Never combine unfinished T-003 work with T-002.
6. The latest instruction authorizes these individual commits when execution
   resumes; no additional commit approval is required for each completed task.
   This planning pass does not commit unfinished implementation. Push and
   deployment are separate actions, not implied by creating local commits.
7. Close an issue only when all its task acceptance gates and final validation
   pass, with links to the relevant commits and evidence. A RED commit/result
   does not mean the feature is delivered. #12 closes only after #41.
8. When tests require Docker, stop this project's Compose services afterward
   with `docker compose down`, without `-v`. Verify temporary schemas and test
   keys are cleaned even after failure; do not stop unrelated workloads.

## Immediate sequence: finish issue #44

### Planning reconciliation before production changes

- Align the current roadmap with closed #47 and the approved #44 scope:
  operator-only first ADMIN plus optional test USER, not reusable account reset.
- Resolve stale conditional/proposed audit and atomicity wording in the high-
  level design using its already recorded owner approvals; preserve quotations.
- Make `04-tasks.md` Test-IDs authoritative for T-003 through T-005 and align
  the low-level scenario catalog without losing any edge cases.
- Correct the implementation log interpretation: T-003 has structural RED
  evidence only. Complete behavioral RED before GREEN, rather than scheduling
  behavioral tests after production implementation. Preserve historical runs.
- Planned persistence correction: introduce
  `infrastructure/persistence/account/PostgresUserAccountRepository.java`,
  implementing the application port through composition with the existing
  Panache repository. Its `long persist(User)` returns the generated ID;
  Panache's `void persist(User)` remains unchanged. Add this file to T-003
  scope and document flushing/unique-constraint translation there.
- Acceptance: no conflicting active bootstrap scope or test-ID mapping;
  the adapter contract is implementable without incompatible Java returns;
  no task is marked done merely by rewriting the plan.
- Evidence: documentation diff, cross-reference review, `git diff --check`.
- Commit boundary: focused planning reconciliation documentation, separately
  from implementation. Suggested message:
  `docs(plan): reconcile account provisioning execution gates #44`.

### T-001: retain and recheck migration acceptance

- AC-IDs: account AC-007. Test-IDs: T-001-T1 through T-001-T3.
- Scope: existing V7-V9 and migration tests; no rewrite of applied migrations.
- Acceptance: clean database migrates through V9; only the three designated
  fixture identities and their dependent sample rows are removed; unrelated
  users and dependents survive; products remain; null hashes are rejected.
- Evidence: existing commit plus fresh migration regressions at T-003/T-005.
- Commit: already delivered as `55d23d5`; do not manufacture a duplicate task
  commit. A discovered regression needs its own focused correction.

### T-002: review and deliver the existing application use case

- Depends on T-001. AC-IDs: account AC-001, AC-002, AC-003, AC-005, AC-010.
- Scope: files already listed under T-002 in the account task list.
- Test-IDs: T-002-T1 through T-002-T6, plus associated domain tests.
- Acceptance: ADMIN-only and ADMIN+USER requests assign fixed roles; existing
  ADMIN, invalid details, and duplicate emails refuse safely; hasher and
  persistence failures propagate through the transaction port without partial
  success; success audit is invoked inside the transaction callback and
  failure audit after it exits exceptionally; Argon2id verification works;
  password-bearing representations redact secrets; login regression passes.
- Evidence: rerun the task's unit/domain/authentication tests and review their
  assertions. Application test doubles do not prove PostgreSQL rollback; that
  proof belongs to T-003. Exclude unfinished T-003 tests from this task's
  completion claim and document the reason for targeted execution.
- Commit: `feat(account): implement atomic bootstrap use case #44 T-002`.

### T-003: implement database adapters after behavioral RED

- Depends on T-002 and planning reconciliation.
- AC-IDs: account AC-001, AC-002, AC-005, AC-011; duplicate-email behavior also
  supports AC-010 and must be added to task traceability.
- Scope: existing T-003 file list plus `PostgresUserAccountRepository.java`;
  CDI registration only when all application ports have implementations.
- Acceptance and test evidence:
  - T-003-T1: coordinate two real PostgreSQL transactions using barriers/latches
    and bounded waits. Prove the second callback cannot enter the protected
    section before the first transaction ends; concurrent bootstraps result
    in exactly one ADMIN and a safe existing-ADMIN refusal. Verify lock release
    after rollback as well as commit; avoid timing-only sleep assertions.
  - T-003-T2: accounts and success audit are visible after commit. Inject a
    success-audit failure and prove that neither requested account persists.
  - T-003-T3: fail after account insertion; prove account rollback, then prove
    the failure audit survives in its separate transaction.
  - T-003-T4: force audit storage failure, capture actual structured logs, and
    assert outcome/reason fields plus absence of sentinel passwords, hashes,
    emails, names, and raw exception/SQL text. Preserve the original failure.
  - T-003-T5: inspect persisted audit rows and schema for the seven approved
    fields and correct outcome, flags, and historical IDs. Account password
    hashes are expected in `users.password_hash`, never in audit or output.
  - T-003-T6 (planned addition): force a database email uniqueness conflict and
    assert safe `DUPLICATE_EMAIL`, no leaked SQL, and no partial accounts.
- Harness acceptance: UUID-isolated schemas, no writes to public application
  rows, usable pgvector type resolution, and cleanup verified after successful
  and failing runs. Investigate the prior leftover-schema cleanup behavior.
- Evidence: behavior-focused RED before production edits, then green PostgreSQL
  integration tests, full `mvnw.cmd test`, and CDI/Hibernate startup validation.
- Commit: `feat(account): add transactional bootstrap persistence #44 T-003`.

### T-004: implement the secure operator command

- Depends on T-003. AC-IDs: account AC-001-AC-005, AC-008, AC-010, AC-011.
- Scope and Test-IDs: existing CLI/configuration files; T-004-T1 through T-004-T5.
- Acceptance: only `--include-test-user` is accepted; passwords are read and
  confirmed without echo; available char buffers are cleared on success and
  failure; unavailable console, mismatched confirmation, and invalid options
  fail before account writes; arguments/environment are not password inputs;
  output uses approved exit codes 0/1/2 without secrets.
- Runtime acceptance: the packaged command invokes the application use case,
  activates required persistence context, starts no HTTP listener, and makes
  no OpenAI requests. The normal HTTP entry point still starts and serves login.
- Evidence: unit tests, command integration tests, packaged process checks,
  and a controlled real-terminal check for hidden input. Mocked prompt tests
  alone cannot prove operating-system terminal echo behavior.
- Commit: `feat(account): add secure operator bootstrap command #44 T-004`.

### T-005: validate and close account provisioning

- Depends on T-004. AC-IDs: all account AC-001 through AC-011.
- Scope and Test-IDs: existing final-validation files; T-005-T1 through T-005-T6.
- Acceptance: clean-install bootstrap creates the requested accounts; both
  authenticate through actual login REST; JWT subject/role and protected
  ADMIN/USER behavior match persisted identities; concurrent/repeated commands
  preserve one initial ADMIN; unsafe input and unavailable dependencies fail
  safely; no public provisioning endpoint is introduced.
- Security evidence: captured output/logs/audit exclude secrets; account rows
  contain verifiable Argon2id hashes only, never plaintext passwords. JWTs
  necessarily exist in login responses; they must not be copied to logs.
- Gates: `mvnw.cmd test`, `mvnw.cmd clean verify`, isolated migration tests,
  AC-to-task-to-test traceability, code/security review with no unresolved
  release-blocking findings, operator instructions, and cleanup verification.
- Deliver validation, traceability, and code-review reports. Record any
  unavailable check as incomplete rather than claiming it passed.
- Commit: `test(account): verify bootstrap and authentication end to end #44 T-005`.
- Close #44 only after this commit and all preceding task gates are satisfied.

## Remaining MVP delivery slices

Each slice below inherits the completion policy. Prepare its reviewed design,
file-level task breakdown, and task-specific test IDs before code. Suggested
commit subjects show scope; add the actual T-NNN identifier for each task.

| Order / issue | Implementation scope | Acceptance criteria / required evidence | Dependency / commit scope |
| --- | --- | --- | --- |
| 1 / #36 | Login form, REST login contract, sessionStorage, logout, auth interceptor. | Valid credentials establish the returned session; invalid credentials and malformed responses leave no new session; logout removes it; passwords are not retained; tokens reach only the configured API origin/path boundary, including lookalike-origin negative tests; unit/service/interceptor tests and frontend build pass. | #44 and #35; `feat(auth): integrate Angular login #36`. |
| 2 / #37 | Shared REST error handling and expired-session UX. | 401 clears stale session; 403 preserves session and displays forbidden; 404/409/422/500 are handled consistently; safe backend codes remain available; internal/provider details are not displayed; test network failure and malformed error envelopes too; tests/build pass. | #36; `feat(frontend): handle REST errors consistently #37`. |
| 3 / #38 | Catalog list/detail backed by existing product API. | Backend-owned values are displayed; loading, empty, not-found, and server/network error states work; no client-controlled price/stock update is sent; tests cover service contracts and views; build passes. Confirm available inactive-product behavior from the real API before designing that state. | #37; `feat(catalog): add customer product views #38`. |
| 4 / #39 | Own orders; list/create/select conversations. | Authenticated requests use the interceptor; no owner ID is supplied as authority; foreign/missing resources show safe 404; new backend-issued conversation ID appears and can be selected; expired session and failed create/list are tested; no fabricated IDs; build passes. | #36, #37, closed #47; `feat(conversation): add customer order and conversation views #39`. Split order and conversation tasks into separate commits. |
| 5 / #40 | Typed chat rendering and interaction states. | Requests require an owned, non-null conversation ID; all five response types and schemaVersion are handled; typed payloads supply business values; text is not parsed for prices/orders; loading, provider/API failure, insufficient knowledge, and unsupported schema are covered; test safe text rendering; define retry semantics without assuming POST idempotency. | #39 and structured-output contracts; `feat(chat): render structured assistant responses #40`. |
| 6 / #41, then #12 | Integrated frontend/backend acceptance and documentation. | Real login-to-catalog-to-conversation-to-chat journey works; own-order and cross-user denial paths work; RAG ready/current scope and no-context paths are verified; frontend tests/build and backend tests/clean verify pass; keyboard navigation, labels, and visible error states are checked; local setup is reproducible; AC traceability and remaining limitations are documented. Distinguish controlled-provider tests from live-provider evidence. | #36-#40, #44, #47; `test(mvp): validate customer journey #41`; close #12 only after #41 passes. |
| 7 / #13 | Correlation, safe logs, metrics, operational instructions. | Demonstrate correlation across relevant request/application logs; sentinel-secret assertions pass; HTTP, database, provider, and ingestion failures/latency are diagnosable; metrics are observable using documented commands; record blind spots; no payment/webhook claims. No new centralized logging platform is assumed. | Validated MVP; `feat(observability): add MVP operational signals #13`. |
| 8 / #14 | Local backend/frontend/database containers and release plan. | A clean documented setup starts all services; health/startup dependencies work; secrets remain external; migration failure and restart behavior are exercised; backup/recovery and forward-migration limitations are documented; stop/start preserves expected data. Produce rollout/rollback plan without executing production deployment. | #13 and #41; `build(containers): package reproducible MVP stack #14`. |
| 9 / #48 | Final supported LTS/runtime compatibility gate. | At execution time verify official support sources, align BOM/plugin/extensions, record exact versions and JDK support; backend clean verify and frontend compatibility tests pass; packaged/containerized login, migrations, and API smoke checks pass. No preselected future version or unrelated feature changes. | #14 and functional MVP complete; `build(deps): align MVP with supported LTS #48`. |
| 10 / #15 | Final security/readiness review and technical handoff. | Full verification on the final baseline passes; configuration, security, database, logs, dependency and operational findings have evidence; classify each remaining item as release blocker, explicitly accepted MVP limitation, or linked follow-up; produce traceability and go/no-go with no unresolved blockers; documentation matches usable behavior. | #48 and all prior MVP gates; `docs(release): record MVP readiness review #15`. |

## Validation prerequisites to resolve in their owning slice

- #39/#41: V7 removes seeded orders, while order creation is outside MVP. Plan
  an isolated test fixture dataset with at least two customer identities and
  owned orders; do not add a production order-creation or provisioning API to
  make tests convenient. Document separate demo-data needs if requested.
- #40/#41: deterministic provider substitutes can verify contracts and failure
  behavior; they cannot establish live retrieval/answer quality. Record which
  evidence was obtained and arrange test-provider configuration when needed.
- #41: choose and document the browser acceptance harness before its tasks;
  do not introduce a test dependency silently just to mark the issue complete.
- #13/#14: select concrete operational tooling/configuration in their designs;
  currently no centralized logging platform or deployment target is approved.

These are explicit design/validation preparation items, not authorization to
expand business scope. Ask the owner only when resolution materially changes
approved product behavior or architecture.

## Deferred scope and release boundary

Refresh tokens (#42), password recovery (#43), self-service registration,
transactional commerce, and centralized log analysis remain outside this MVP.
Reassess the production authentication lifecycle before a public release.
This plan authorizes no deployment and invents no checkout, payment, stock,
returns, shipping, or notification implementation tasks.

## Planning-pass completion criteria

- Every remaining MVP issue has dependencies, measurable acceptance, evidence,
  and a commit boundary in this plan.
- Existing T-001/T-002 work is distinguished from unfinished T-003/T-004/T-005.
- RED deficiencies and the Panache return-type conflict have a planned repair.
- No production source, test implementation, migration, or secret is changed.
- No task/issue is marked completed based only on this document.
