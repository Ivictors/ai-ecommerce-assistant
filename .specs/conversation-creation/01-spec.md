# Product Intent Specification — Authenticated Conversation Creation

## Source

- Tracker: GitHub
- ID: #47 — `[SLDD] Create conversations for authenticated users`
- URL: https://github.com/Ivictors/ai-ecommerce-assistant/issues/47
- Snapshot date: 2026-10-05

## Goal

Allow an authenticated customer to start a conversation from the client and
receive a conversation identifier owned by that customer. This removes the
current need to seed or manually supply an identifier before the customer can
use the chat endpoint.

## Target Users

- Authenticated customer with the `USER` role who wants to start an assistant
  conversation.
- Angular client that needs a backend-issued conversation identifier before
  sending a chat message.

## Success Metrics

- An authenticated customer can create and list a conversation through REST.
- The returned identifier can be used as the required `conversationId` for
  the existing chat endpoint.
- Unauthenticated callers create no conversation.
- Ownership tests prove that one customer cannot use another customer’s
  conversation.

## Acceptance Criteria (EARS-lite)

- AC-001: When an authenticated `USER` requests a new conversation, the system
  shall create a conversation owned by the identity in the verified access
  token and return its identifier and existing conversation metadata.
- AC-002: When the creator lists their conversations, the system shall include
  the newly created conversation.
- AC-003: If an unauthenticated caller requests conversation creation, then the
  system shall reject the request without creating a conversation. A validly
  signed identity with no matching persisted user is also unauthenticated for
  this operation and shall not create a conversation.
- AC-004: If a client attempts to choose an owner identity for a new
  conversation, then the system shall not use that client-supplied identity
  and shall associate the conversation only with the verified caller.
- AC-005: If an authenticated customer uses another customer’s conversation
  identifier with the chat operation, then the system shall return the
  existing not-found behavior without exposing the conversation.
- AC-006: When the creator supplies the returned identifier to the existing
  chat operation, the system shall pass the existing owner-scoped conversation
  lookup.

## Non-Goals

- Persisting chat message history beyond the existing conversation/memory
  behavior.
- Conversation title, rename, deletion, sharing, or transfer.
- Listing or accessing another customer’s conversations.
- Changing the chat response envelope or the rule that `conversationId` is
  required.
- Exposing arbitrary `userId` as a request parameter or owner-selection field.
- Calling the external LLM as part of conversation-creation tests.

## Non-Functional Requirements

- Authentication and authorization are enforced server-side.
- Ownership comes exclusively from the verified access-token identity.
- The operation must not log tokens or unnecessary personal data.
- No quantitative latency or availability target has been approved for this
  operation.

## Glossary

- **Conversation** — A persisted assistant-session record associated with one
  authenticated user.
- **Conversation identifier** — The backend-generated numeric ID required by
  the existing chat endpoint.
- **Verified caller** — The identity derived by the server from a validated
  access token.
- **Owner** — The user associated with the persisted conversation.

## Risks and Assumptions

- Issue #35 is complete and provides JWT verification and numeric subject
  identity.
- Existing `GET /api/conversations` returns only conversations for the
  authenticated user.
- Existing `POST /api/chat` requires a non-null ID and checks conversation
  ownership before assistant processing.
- The existing `conversations` table and `ConversationResponse` are reused;
  no schema change is expected.
- #44 remains necessary for real provisioned MVP users, but isolated tests for
  this API can use test-only users and signed tokens.

## Open Questions

None. The parent issue and approved authentication/ownership contracts define
the behavior needed for this endpoint.

## Resolved Questions

None.
