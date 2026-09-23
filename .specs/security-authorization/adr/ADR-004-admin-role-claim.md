# ADR-004: Identify administrators through the verified role claim

## Status
Accepted

## Context
The first milestone must protect existing catalog mutations without creating an unapproved administration model.

## Decision
The administrator will be identified by the verified `role` claim with value `ADMIN`. The backend will never accept a role from the body, query, or frontend.

## Consequences
The JWT provider must issue and sign this claim; future changes to persisted roles require a new decision and review.
