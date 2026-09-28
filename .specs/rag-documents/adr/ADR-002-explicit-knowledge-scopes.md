# ADR-002: Store an explicit knowledge scope on each document version

## Status

Accepted

## Context

Retrieval must not disclose knowledge outside its approved audience. Per-user
ownership is explicitly outside the first delivery, while public and protected
knowledge need a deterministic boundary.

## Decision

Each document version stores an explicit knowledge scope. Retrieval filters by
scope before content is passed to the assistant. Document management remains
ADMIN-only.

## Consequences

The first model supports public and protected knowledge without implementing
user-owned documents. Scope filtering becomes a security-critical retrieval
test boundary.
