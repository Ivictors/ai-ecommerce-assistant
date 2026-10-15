# Angular Product Catalog

## Source

- Tracker: GitHub issue #38
- Snapshot date: 2026-10-15

## Acceptance Criteria

- AC-001: The client shall load the catalog from `GET /api/products`.
- AC-002: The client shall display product values returned by the backend.
- AC-003: When the catalog is empty, the client shall display an empty state.
- AC-004: When a product is inaccessible, the client shall display a not-found state.
- AC-005: The client shall not send price or stock as authoritative update data.

## Non-goals

- Product creation, update, deletion, cart, checkout, or client-side price calculation.
