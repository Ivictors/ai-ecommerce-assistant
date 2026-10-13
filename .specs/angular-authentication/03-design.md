# High-Level Technical Design — Angular Authentication Session

## Architecture

```text
LoginComponent
      |
      v
AuthService -----> POST /api/auth/login -----> Backend AuthenticationResource
      |
      v
sessionStorage
      ^
      |
AuthInterceptor -----> protected backend requests with Bearer token
      ^
      |
AuthGuard -----> route experience only; backend remains authoritative
```

## Component Responsibilities

- `LoginComponent`: collects credentials, renders validation/loading/safe failure state, and navigates after successful authentication.
- `AuthService`: owns the login request, token storage, session clearing, and session lookup.
- `authInterceptor`: adds a bearer header only to the configured backend API boundary.
- `authGuard`: prevents unauthenticated navigation to the private shell; it is not an authorization boundary.
- `ApiError`: preserves safe backend error codes for later feature-specific handling without exposing internal details.

## API Contract Sketch

`POST /api/auth/login`

Request:

```json
{"email":"customer@example.com","password":"secret"}
```

Success `200`:

```json
{"accessToken":"<jwt>","tokenType":"Bearer","expiresIn":900}
```

Authentication failure `401`: safe error response.

Malformed request `400`: safe error response.

The frontend must reject a `200` response without a non-empty `accessToken`.

## Data Flow

1. The login form validates required fields locally.
2. `AuthService` sends the credentials to the backend.
3. The backend verifies Argon2id credentials and returns a signed access token.
4. `AuthService` stores only the token value in `sessionStorage`.
5. The router navigates to `/private`.
6. The interceptor reads the token for approved backend requests.
7. Logout removes the token and returns the user to the login route.

## Security Posture

- Passwords exist only in the login request and are never stored by Angular.
- Tokens are stored in `sessionStorage` according to the approved MVP decision.
- The interceptor uses an explicit backend-boundary predicate; it never forwards tokens to unrelated origins.
- Angular guards improve UX but do not replace server-side JWT verification, role checks, or ownership checks.
- No token, password, or private key is written to logs.

## Observability Requirements

- Do not log credential or token values.
- Preserve safe status/error codes for the global error-handling slice #37.
- Authentication telemetry is deferred to issue #13.

## Trade-offs

- `sessionStorage` limits persistence after the tab closes, reducing long-lived browser exposure, but requires login again in a new tab/session.
- A functional interceptor predicate is explicit and testable, but must be updated if the API deployment boundary changes.
- A local route guard improves navigation, but cannot protect data; every backend resource remains responsible for authorization.
- No refresh token keeps the MVP small but means an expired access token requires a new login.

## Risks and Rollback

- If the frontend contract or interceptor is wrong, revert the task commit without database changes.
- If the API base configuration changes, update the boundary predicate and its tests before changing deployment configuration.
- XSS risk associated with browser-readable storage remains a documented MVP limitation for later hardening.

## Test Scenario Map

- valid login stores the access token;
- invalid credentials leave no session;
- malformed successful response fails closed;
- logout removes the token;
- authenticated API request receives Bearer header;
- unrelated-origin request does not receive Bearer header;
- missing token does not receive Bearer header;
- protected route redirects without a session;
- successful login navigates to `/private`.

## Open Questions

None.
