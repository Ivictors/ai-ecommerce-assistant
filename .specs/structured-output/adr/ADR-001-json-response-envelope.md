# ADR-001: Use one JSON response envelope for chat

## Status

Accepted

## Context

The chat currently returns plain text, but future frontend flows need
predictable product, order, and policy data while general conversation should
remain natural language.

## Decision

The single `/api/chat` endpoint returns `application/json` with `type`,
`schemaVersion`, `message`, and an optional typed `data` payload. General
conversation uses `type=TEXT`.

## Consequences

The API changes from `text/plain` to JSON, requiring client contract updates.
In exchange, one endpoint supports both free-form and structured flows without
requiring frontend text parsing or parallel API versions.
