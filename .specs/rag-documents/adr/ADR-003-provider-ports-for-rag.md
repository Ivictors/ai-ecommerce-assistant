# ADR-003: Isolate extraction and embeddings behind application ports

## Status

Accepted

## Context

PDF parsing and embedding generation depend on external libraries or providers
that may change, fail, or be unavailable during unit tests.

## Decision

The application processing workflow depends on ports for text extraction and
embedding generation. Infrastructure provides the initial PDF/text and OpenAI
adapters. Unit tests use deterministic fakes or mocks.

## Consequences

Business orchestration remains testable without a live OpenAI key and future
providers can be introduced without changing the document lifecycle.
