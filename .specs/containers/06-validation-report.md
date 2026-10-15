# MVP Container Validation

## Scope

Issue #14 provides a reproducible three-service MVP stack:

- PostgreSQL with pgvector;
- the Quarkus JVM image built from the packaged `target/quarkus-app` layout;
- the Angular production bundle served by Nginx, including SPA fallback and an
  `/api/` reverse proxy to the backend.

The stack is defined in `docker-compose.mvp.yml` so the existing PostgreSQL
development compose file remains compatible with the test workflow.

## Required runtime inputs

The compose file intentionally requires `POSTGRES_DB`, `POSTGRES_USER`,
`POSTGRES_PASSWORD`, `OPENAI_API_KEY`, and `JWT_KEYS_DIR` from the operator's
environment or an untracked `.env` file. Secrets are not given defaults and
are not copied into images.

Before building the backend image, run the Maven package phase so
`target/quarkus-app` exists. The Quarkus readiness endpoint is
`/q/health/ready`; it is the operational gate for the backend container.

## Rollback boundary

The MVP rollback is image/tag and compose-file based: stop the current stack,
select the previous application image or commit, and start it with the same
externalized configuration. Database volumes are not removed by the normal
shutdown command.
