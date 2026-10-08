# ADR-005: Preserve the MVP Framework Baseline Until Official Delivery

## Status

Accepted

## Context

The repository currently pins Quarkus 3.39.3 and Java 21. Quarkus 3.39
maintenance ended on 2026-09-30, while Quarkus 3.40.1 is the current LTS as of
2026-10-07. Updating the framework now would expand the approved MVP task and
mix dependency maintenance with account provisioning.

## Decision

Keep the current Quarkus 3.39.3 and Java 21 baseline for this MVP. Do not
upgrade framework or runtime dependencies as part of issue #44. Issue #48 is
the final technical compatibility gate within MVP: after #14 and before the
final review in #15, update Quarkus to the then-current supported LTS and
verify the chosen JDK distribution's support/update policy.

## Consequences

Issue #44 remains narrow and the MVP is implemented against its existing
baseline. Quarkus 3.39 is outside its maintenance window, so this is an
explicitly temporary MVP exception, not a production recommendation. The
issue #48 must include the framework/JDK update and full validation before
the final review; no public release should rely on this exception.
