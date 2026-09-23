# ADR-003: Return 404 for another user's private resources

## Status
Accepted

## Context
A `403` may reveal that a resource identifier exists even when the client has no access.

## Decision
Orders, conversations, and other private resources belonging to another customer will be treated as not found and return `404 Not Found`.

## Consequences
This reduces resource-existence disclosure, but clients cannot distinguish a missing resource from one belonging to another user.
