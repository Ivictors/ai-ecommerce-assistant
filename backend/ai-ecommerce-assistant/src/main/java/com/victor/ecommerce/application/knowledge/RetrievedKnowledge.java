package com.victor.ecommerce.application.knowledge;

public record RetrievedKnowledge(Long documentId, Long versionId, Integer chunkIndex, String content) {
}
