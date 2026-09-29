# ADR-002: Build typed payloads from authoritative application data

## Status

Accepted

## Context

The LLM can classify a request but must not invent product prices, stock,
order status, ownership, payment state, or authorization.

## Decision

The application validates the intent, invokes the appropriate secured
application capability, and builds the public response DTO from its result.
Model-generated business values are never copied directly into the payload.

## Consequences

Typed responses require application mapping and validation, but preserve domain
truth and existing authorization boundaries.
