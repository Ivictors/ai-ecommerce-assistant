# ADR-001: Use synchronous processing for the first delivery

## Status

Accepted

## Context

The first RAG delivery needs a simple, deterministic workflow. Introducing a
queue or worker before the document lifecycle is proven would add operational
complexity that is outside the approved scope.

## Decision

Document processing starts through an explicit administrator command and runs
synchronously. The application lifecycle and ports must not prevent a future
worker from invoking the same processing use case.

## Consequences

The first delivery is easier to test and operate, but large documents may make
the request long-running. Production evolution may move execution to a worker
without changing the document state model or business contract.
