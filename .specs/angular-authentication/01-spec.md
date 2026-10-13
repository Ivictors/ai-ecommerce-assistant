# Product Intent Specification — Angular Authentication Session

## Source

- Tracker: GitHub
- ID: #36 — `[SLDD] Integrate Angular session with backend login #35`
- URL: https://github.com/Ivictors/ai-ecommerce-assistant/issues/36
- Snapshot date: 2026-10-13

## Goal

Allow a provisioned customer or administrator to authenticate through the Angular application using the existing backend login contract, maintain a browser-session access token, and use that session for protected API requests without exposing credentials or trusting client-side guards as authorization.

## Target Users

- Provisioned customer using the Angular application.
- Provisioned administrator using the Angular application.

## Success Metrics

- Valid backend credentials establish an Angular session with the returned access token.
- Invalid credentials do not create a session and show a uniform safe message.
- Logout removes the access token from browser session storage.
- Protected API requests send the bearer token only to the configured backend API boundary.
- Angular unit tests and production build pass.

## Acceptance Criteria (EARS-lite)

- AC-001: When valid credentials are submitted, the application shall call `POST /api/auth/login` and store only the returned access token in `sessionStorage`.
- AC-002: If backend authentication fails, then the application shall not create or retain a new session and shall display a safe uniform error.
- AC-003: When the user logs out, the application shall remove the access token from `sessionStorage`.
- AC-004: When a protected API request is made with an active session, the application shall send the access token as a Bearer authorization header to the configured backend API boundary.
- AC-005: If a request targets an unrelated origin or the session has no access token, then the application shall not send a bearer authorization header.
- AC-006: If the login response does not contain a non-empty access token, then the application shall treat authentication as failed and shall not establish a session.
- AC-007: When an unauthenticated user navigates to a protected route, the application shall navigate to the login experience.
- AC-008: The application shall not persist passwords, private keys, or access tokens in application logs.

## Non-Goals

- Refresh tokens or silent token renewal; tracked by issue #42.
- Password recovery; tracked by issue #43.
- User registration, password change, MFA, OAuth, or social login.
- Client-side authorization decisions replacing backend authorization.
- Remembering the access token beyond the browser tab session.

## Non-Functional Requirements

- The session storage key shall be stable and application-specific.
- A malformed login response shall fail closed without storing a token.
- The Angular production build and unit test suite shall pass before completion.

## Glossary

- **Access token** — Short-lived backend-issued JWT used for API authentication.
- **Session** — Browser-tab authentication state represented by the access token in `sessionStorage`.
- **Configured API boundary** — The approved backend origin/path to which bearer credentials may be sent.
- **Protected route** — Angular route requiring a local authenticated-session check for user experience.

## Risks and Assumptions

- The backend contract is already approved: email/password request and `accessToken`, `tokenType`, `expiresIn` response.
- Browser storage is readable by JavaScript; XSS protection remains a separate hardening concern.
- Backend authorization remains authoritative even when the Angular guard allows navigation.

## Resolved Questions

- **Q-001:** Where should the MVP access token be stored?
  - Resolution: `sessionStorage` only.
  - Resolved at: 2026-10-13
- **Q-002:** What should happen after successful login?
  - Resolution: navigate to `/private` for the current MVP shell.
  - Resolved at: 2026-10-13
- **Q-003:** What should happen when a response is malformed or authentication returns `401`?
  - Resolution: fail safely, do not create/retain the session, and use a uniform authentication message.
  - Resolved at: 2026-10-13
- **Q-004:** Which requests may receive the bearer token?
  - Resolution: only requests to the configured backend API boundary; unrelated origins must never receive it.
  - Resolved at: 2026-10-13

## Open Questions

None.
