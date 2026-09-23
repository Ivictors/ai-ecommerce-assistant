# ADR-002: Enforce ownership in application use cases

## Status
Accepted

## Context
REST annotations protect the entry point, but they do not guarantee that a loaded resource belongs to the authenticated user or protect internal/Tool calls.

## Decision
Ownership will be validated in application services/use cases, using the authenticated identity and owner-filtered queries where possible.

## Consequences
The control applies to REST and AI, but requires each use case to declare its user context explicitly.
