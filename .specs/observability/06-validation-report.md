# MVP Observability Validation

## Scope

Issue #13 establishes the smallest operational baseline needed by the MVP:

- Quarkus SmallRye Health exposes liveness and readiness endpoints;
- every REST response receives an `X-Request-ID` correlation identifier;
- a safe client identifier is preserved only when it matches the bounded format
  `[A-Za-z0-9._-]{1,100}`;
- unsafe or absent identifiers are replaced with a generated UUID;
- passwords, tokens, request bodies, and arbitrary header values are not logged
  by this feature.

## Validation

The filter tests prove propagation and header-injection protection. The complete
backend validation is executed with `mvnw.cmd clean verify` while PostgreSQL and
pgvector are available. Health endpoint smoke checks are part of the deployment
stack validation and must use `/q/health/live` and `/q/health/ready`.

## Operational boundary

This MVP does not introduce a log aggregation vendor, distributed tracing, or
business metrics. Those are production improvements and require an explicit
capacity, retention, and privacy decision before implementation.
