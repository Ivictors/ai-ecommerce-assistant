# Angular Orders and Conversations

## Source

- Tracker: GitHub issue #39
- Snapshot date: 2026-10-15

## Acceptance Criteria

- AC-001: The client shall load the authenticated user's orders from `GET /api/orders`.
- AC-002: The client shall load and create conversations through the backend conversation API.
- AC-003: The client shall use only backend-issued conversation IDs.
- AC-004: The client shall not send a client-supplied owner identifier as authority.
- AC-005: Missing or inaccessible resources shall use safe error handling.

## Non-goals

- Checkout, order creation, payment, order mutation, or client-side authorization.
