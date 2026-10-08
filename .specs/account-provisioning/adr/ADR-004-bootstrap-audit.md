# ADR-004: Audit Bootstrap Outcomes Without Credentials

## Status

Accepted

## Context

Issue #44 requires successful and failed provisioning attempts to be audited
without secrets. A one-time operator command has no existing audit facility,
and a failed account transaction may roll back along with any audit row written
inside it.

## Decision

Propose a minimal PostgreSQL audit record for each bootstrap outcome, plus a
structured safe log fallback if persistence of the audit record is unavailable.
Commit a successful audit record in the same transaction as the created
account(s); after a failed account transaction rolls back, record the failure
in a separate transaction. Store only timestamp, outcome, a non-sensitive
reason code, and minimal operation metadata; do not store passwords, hashes,
or raw secret input. The product owner approved the PostgreSQL audit table plus
safe structured-log fallback on 2026-10-07. A future log-mapping/analysis tool
is explicitly deferred.

## Consequences

Database-backed records are queryable and survive process exit, unlike
console-only output, but add a table, migration, and retention consideration.
The two transaction paths ensure successful audit records cannot diverge from
committed accounts while failed outcomes survive account rollback. The log
fallback cannot guarantee durable auditing when the database and log
collector are both unavailable. The audit table adds a small schema and
retention consideration; centralized log mapping remains future work.
