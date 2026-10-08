# ADR-001: Operator-Only Quarkus Command Bootstrap

## Status

Accepted

## Context

The first ADMIN cannot be created through an ADMIN-protected endpoint because
no administrator exists yet. A public bootstrap endpoint would expose a
privileged account-creation path. The project already uses Quarkus and wants
to preserve its own login/JWT flow rather than integrate Keycloak as an
identity provider.

## Decision

Provide a dedicated operator-controlled Quarkus command-mode entry point for
initial account bootstrap. Select it separately from the normal HTTP
application entry point, disable HTTP hosting and AI-provider calls for that
execution, and collect passwords through hidden interactive terminal input.
Do not add an HTTP provisioning route or accept passwords in process arguments
or environment variables.

## Consequences

Bootstrap requires operator access to the runtime and an interactive terminal.
The command can reuse CDI, the existing application services, persistence,
Flyway, and Argon2id implementation without changing user login. Build and
runtime configuration must prove that the command does not listen on HTTP or
call OpenAI. Unattended bootstrap is not supported in the MVP.
