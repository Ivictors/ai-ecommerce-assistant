# Angular REST Error Handling

## Source

- Tracker: GitHub issue #37
- Snapshot date: 2026-10-15

## Scope

The Angular client shall handle backend HTTP failures consistently while preserving safe backend error codes for feature-specific behavior. A `401` clears the stale browser session and redirects to login; a `403` preserves the session and displays a forbidden state.

## Acceptance Criteria

- AC-001: When the backend returns `401`, the client shall clear the session and navigate to `/login`.
- AC-002: When the backend returns `403`, the client shall preserve the session and publish a safe forbidden message.
- AC-003: When the backend returns `404`, `409`, `422`, or `500`, the client shall publish a status-specific safe message.
- AC-004: The client shall preserve a backend error code without displaying internal details.

## Non-goals

- Retry policies, refresh tokens, centralized logging, and feature-specific page redesigns.

## Resolved Questions

- Error handling uses a functional Angular HTTP interceptor and a shared signal-based error service.
