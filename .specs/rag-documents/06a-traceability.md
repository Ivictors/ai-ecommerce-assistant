# Traceability — RAG and Knowledge Documents

| AC-ID | Task(s) | Tests | Code symbols | Gate |
|---|---|---|---|---|
| AC-001 | T-001, T-002, T-003 | `KnowledgeDocumentServiceTest.createsDraftForSupportedText` | `KnowledgeDocument`, `KnowledgeDocumentVersion`, `KnowledgeDocumentService` | PASS |
| AC-002 | T-004, T-005 | `DocumentProcessingServiceTest.processesTextIntoReadyDocument` | `DocumentProcessingService`, `DocumentChunk`, `KnowledgeChunkRepository` | PASS |
| AC-003 | T-006, T-007 | `ChatApplicationServiceTest.sendsRetrievedKnowledgeContextToAssistant` | `ScopedKnowledgeRetriever`, `ChatApplicationService` | PASS |
| AC-004 | T-007 | `ChatApplicationServiceTest` no-context path | `ChatApplicationService` safe context instruction | PASS |
| AC-005 | T-006 | `ScopedKnowledgeRetrieverTest` | `RetrievedKnowledge`, native retrieval projection | PASS |
| AC-006 | T-007 | existing `ProductToolTest`, `OrderToolTest`, chat boundary test | `ProductTool`, `OrderTool`, `ChatApplicationService` | PASS |
| AC-007 | T-001, T-005 | `DocumentProcessingServiceTest.marksFailedWhenExtractionFails` | lifecycle transitions and processing exception mapping | PASS |
| AC-008 | T-003, T-006 | scoped retrieval test and authorization matrix | `KnowledgeScope`, `KnowledgeChunkRepository.searchSimilar` | PASS |

## Orphan Review

All new production symbols belong to the document lifecycle, processing,
retrieval, or assistant-integration tasks. No unrelated production change was
identified. Tests use deterministic ports and do not require a live OpenAI key.
