# Product Intent Specification — RAG and Documents

## Source

- Tracker: GitHub
- ID: #18 — `[SLDD-01] Specify RAG and knowledge documents`
- URL: https://github.com/Ivictors/ai-ecommerce-assistant/issues/18
- Snapshot date: 2026-09-28

## Goal

Enable the assistant to answer questions about approved e-commerce documents
using retrieved source content, while clearly distinguishing document-based
knowledge from authoritative transactional data. Customers should receive
answers grounded in the application's approved knowledge sources instead of
answers invented by the language model.

## Target Users

- Authenticated customer asking about company policies or product knowledge.
- Unauthenticated visitor asking about publicly available knowledge, if the
  existing endpoint permits that interaction.
- Administrator responsible for supplying approved knowledge documents.

## Success Metrics

- Every document-based answer can be traced to retrieved approved content in
  automated tests.
- The assistant does not present an answer as grounded when no relevant
  approved content is available.
- Transactional values such as current price, stock, order status, ownership,
  payment state, and authorization continue to come from application services
  and tools rather than document retrieval.
- Documents with invalid or unsupported content are not silently indexed as
  usable knowledge.

## Acceptance Criteria (EARS-lite)

- AC-001: The system shall represent an approved knowledge document with a
  stable identifier, source metadata, content, and processing state.
- AC-002: When an approved document is processed successfully, the system
  shall make its searchable content available to the retrieval flow.
- AC-003: When a user asks a question covered by approved knowledge, the
  system shall provide retrieved content to the assistant before generating
  the answer.
- AC-004: If no relevant approved content is available, then the system shall
  tell the user that it does not have enough knowledge to answer rather than
  presenting an unsupported policy claim.
- AC-005: The system shall preserve the source identity of retrieved content
  so that an answer can be associated with the document that supplied it.
- AC-006: The system shall not use document retrieval as the authoritative
  source for current price, stock, order status, payment state, ownership, or
  authorization.
- AC-007: If document processing fails, then the system shall keep the failed
  document unavailable for normal retrieval and expose a deterministic failure
  state for operational handling.
- AC-008: The system shall prevent content belonging to an unauthorized
  knowledge scope from being returned to a user.

## Non-Goals

- Implementing the complete administrator document-management API.
- Defining payment, inventory, promotion, return, or order workflows.
- Replacing ProductTool or OrderTool with document retrieval.
- Allowing the language model to create, approve, or modify authoritative
  e-commerce state.
- Supporting every possible file format before an approved format set exists.
- Defining production deployment, reindexing operations, or a background job
  platform before their behavior is approved.

## Non-Functional Requirements

No quantitative non-functional requirements are approved for the first
delivery. Retrieval latency, document size limits, indexing throughput,
retention, and availability are explicitly deferred to the production
evolution plan and are not implementation gates for this phase.

## Glossary

- **RAG** — Retrieval-Augmented Generation: retrieving approved content before
  generating an assistant response.
- **Knowledge document** — A source approved for use as non-transactional
  application knowledge.
- **Chunk** — A searchable segment derived from a knowledge document.
- **Embedding** — A vector representation used to compare semantic similarity.
- **Retriever** — The component that selects relevant indexed chunks for a
  question.
- **Knowledge scope** — The audience or authorization boundary associated with
  a document.
- **Authoritative transactional data** — Current business state supplied by
  application services and trusted integrations.

## Risks and Assumptions

- The existing database already enables the pgvector extension, but no RAG
  schema or vector mapping is approved yet.
- LangChain4j/OpenAI is already present, but embedding-model and vector-store
  choices remain unspecified.
- The current assistant prompt requires company policies to come from
  available knowledge sources, but it does not define document lifecycle or
  retrieval failure behavior.
- Authorization for private knowledge could expose sensitive business
  content if scope rules are not defined before implementation.

## Resolved Questions

- **Q-001:** Which document sources and formats are in the first delivery?
  - Resolution: "Upload de PDF e arquivos de texto".
  - Interpretation: The first delivery accepts PDF and text-file uploads. It
    does not consume external URLs.
  - Resolved at: 2026-09-28

- **Q-002:** Who may create, replace, publish, unpublish, and delete knowledge
  documents?
  - Resolution: "apenas role admin".
  - Interpretation: All document-management operations require the `ADMIN`
    role.
  - Resolved at: 2026-09-28

- **Q-003:** Which knowledge is public and which requires authenticated-user or
  administrator scope?
  - Resolution: "escopos explicitos".
  - Interpretation: Each document has an explicit knowledge scope. User-owned
    documents are outside the first delivery.
  - Resolved at: 2026-09-28

- **Q-004:** What operation starts indexing and how are updates handled?
  - Resolution: "criação do documento separada do processamento; estados DRAFT,
    PROCESSING, READY, FAILED e UNPUBLISHED; processamento síncrono quando o
    administrador solicitar".
  - Interpretation: Upload creates a draft. An explicit administrator command
    starts synchronous processing and moves the document through the approved
    states. Background jobs are outside the first delivery.
  - Resolved at: 2026-09-28

- **Q-005:** What should happen when a document is replaced or deleted?
  - Resolution: "versionamento - e despublicação antes de remoção física".
  - Interpretation: Documents are versioned. A document must be unpublished
    before physical removal, and inactive versions must not be retrieved.
  - Resolved at: 2026-09-28

- **Q-006:** Which embedding model, chunking policy, similarity threshold, and
  result limit are approved?
  - Resolution: "vamos seguir esta ordem, inicialmente de forma simples como
    planejado, depois quando necessario implementaremos esse passo para
    produção".
  - Interpretation: Initial technical defaults may be selected in the design
    for a simple implementation. Production-scale processing, retries,
    asynchronous execution, and operational hardening are future evolution,
    not first-delivery requirements.
  - Resolved at: 2026-09-28

- **Q-007:** Should retrieval citations be exposed in the REST response or only
  used internally by the assistant?
  - Resolution: "fontes mantidas internamente no primeiro momento".
  - Interpretation: Retrieval source metadata is retained internally and is not
    exposed in the first REST response contract.
  - Resolved at: 2026-09-28

## Open Questions

None. All questions from the initial draft are resolved above. The first delivery
intentionally excludes external URLs, user-owned documents, and asynchronous
processing. Production improvements remain future evolution items and are not
required for the initial implementation.
