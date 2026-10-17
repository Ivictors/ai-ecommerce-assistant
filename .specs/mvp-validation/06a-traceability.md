# MVP Traceability

| MVP area | Evidence |
|---|---|
| Authentication | #35, #44, #36; backend and Angular tests |
| REST errors | #37; 401/403/404/409/422/500 mapping tests |
| Catalog | #38; list/detail tests |
| Orders/conversations | #39; ownership and backend-issued ID tests |
| Structured chat | #40; five response variants and schema tests |
| Backend security/RAG | `mvnw.cmd clean verify` and existing integration tests |
| Operational baseline | #13, #14, #15, and #48; health, correlation IDs, containers, LTS, and hardening evidence |
| Dependency security | npm full and production-only audits; zero vulnerabilities |

## Explicit validation limits

- Browser manual acceptance and live OpenAI/RAG answer quality are not proven
  by the automated suite; issue #50 owns that evidence.
- Failsafe integration-test execution remains disabled by the current Maven
  MVP profile; issue #52 owns the harness decision.
- No production deployment was performed.
