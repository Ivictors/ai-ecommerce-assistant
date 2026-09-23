# ADR-001: Use JWT `sub` as the numeric user identity

## Status
Accepted

## Context
The backend must associate the authenticated request with the persisted `User` without trusting identifiers supplied by the client.

## Decision
The verified `sub` claim will contain the numeric identifier of the current `User`.

## Consequences
The mapping is direct and avoids lookups by mutable data, but tokens with an invalid `sub` must be rejected and the provider configuration must preserve this contract.
