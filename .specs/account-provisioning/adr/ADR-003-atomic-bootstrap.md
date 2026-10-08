# ADR-003: Atomic Initial Account Bootstrap

## Status

Accepted

## Context

The operator may request both the first ADMIN and a test USER in one bootstrap
operation. Partial success would leave the operator unsure whether the
bootstrap can be safely retried. Concurrent invocations could also race on
the initial ADMIN check.

## Decision

Create the requested ADMIN and optional USER in one database transaction. If
any requested account cannot be created, roll back all account changes. Guard
the no-existing-ADMIN check and writes with a PostgreSQL transaction-level
serialization mechanism so only one competing bootstrap can succeed.

## Consequences

The result is deterministic and safely retryable after failure, and
concurrent invocations cannot both establish the initial ADMIN. The audit
design must record failed outcomes outside the rolled-back account transaction
or through a safe fallback. The serialization mechanism adds a PostgreSQL-
specific infrastructure concern. The product owner approved this behavior on
2026-10-07.
