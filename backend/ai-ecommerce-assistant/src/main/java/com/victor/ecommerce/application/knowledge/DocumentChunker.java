package com.victor.ecommerce.application.knowledge;

import java.util.List;

public interface DocumentChunker {
    List<String> chunk(String text);
}
