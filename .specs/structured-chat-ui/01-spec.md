# Angular Structured Chat UI

## Source

- Tracker: GitHub issue #40
- Snapshot date: 2026-10-15

## Acceptance Criteria

- AC-001: The client shall require a positive backend-issued `conversationId` before sending a chat request.
- AC-002: The client shall render `TEXT`, `PRODUCT_INFORMATION`, `ORDER_STATUS`, `POLICY_INFORMATION`, and `FALLBACK` responses by their typed `type`.
- AC-003: The client shall reject unsupported `schemaVersion` values without rendering authoritative data.
- AC-004: The client shall render product and order values from typed response data and shall not parse message text for business values.
- AC-005: The client shall expose loading, failure, retry, and no-context states.

## Non-goals

- Automatic retries, refresh tokens, conversation ownership decisions, or LLM-side business authority.
