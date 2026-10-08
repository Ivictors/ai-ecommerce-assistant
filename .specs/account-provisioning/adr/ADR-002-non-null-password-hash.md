# ADR-002: Remove Passwordless Test Fixtures and Require Credential Hashes

## Status

Accepted

## Context

V3 seeds three test users and dependent orders/conversations without
credentials. V6 adds nullable `password_hash`. The product owner has decided
these identities and related data are disposable test fixtures and that
persisted application users must not have a null password hash. V3 and V6 are
already-applied migrations and must remain unchanged.

## Decision

Add forward Flyway migrations after V6: first delete only the exact designated
test accounts and their dependent conversations, order items, and orders;
then add a NOT NULL constraint to `users.password_hash`. Update the JPA/domain
model and all fixtures to require a hash. Do not delete users using a broad
null-hash predicate.

## Consequences

Fresh databases retain product fixtures but no longer retain the seeded test
accounts or their sample orders/conversations. Existing data-bearing databases
must be backed up and the exact fixture identities verified before migration.
Any test or development SQL that inserts a user without a hash must be updated.
Unintended deletion can be recovered only from a backup; the migration is not
reversed by editing V3/V6.
