package com.victor.ecommerce.application.knowledge;

import java.util.List;

public interface KnowledgeRetriever {
    List<RetrievedKnowledge> retrieve(String question);
}
