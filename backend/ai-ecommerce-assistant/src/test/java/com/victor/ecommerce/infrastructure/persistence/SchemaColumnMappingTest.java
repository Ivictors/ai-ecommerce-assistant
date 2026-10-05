package com.victor.ecommerce.infrastructure.persistence;

import com.victor.ecommerce.domain.knowledge.KnowledgeDocument;
import com.victor.ecommerce.domain.knowledge.KnowledgeDocumentVersion;
import com.victor.ecommerce.domain.order.Order;
import com.victor.ecommerce.domain.product.Product;
import com.victor.ecommerce.domain.user.User;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaColumnMappingTest {

    @Test
    void knowledgeDocumentTimestampMustMapToFlywayColumn() throws NoSuchFieldException {
        assertColumn(KnowledgeDocument.class, "createdAt", "created_at", 255, "");
    }

    @Test
    void knowledgeVersionColumnsMustMatchFlywayDefinitions() throws NoSuchFieldException {
        assertColumn(KnowledgeDocumentVersion.class, "mediaType", "media_type", 150, "");
        assertColumn(KnowledgeDocumentVersion.class, "scope", "", 30, "");
        assertColumn(KnowledgeDocumentVersion.class, "state", "", 30, "");
        assertColumn(KnowledgeDocumentVersion.class, "failureReason", "failure_reason", 255, "TEXT");
    }

    @Test
    void existingDomainStringColumnsMustMatchFlywayDefinitions() throws NoSuchFieldException {
        assertColumn(Product.class, "name", "", 150, "");
        assertColumn(Order.class, "status", "", 30, "");
        assertColumn(User.class, "name", "", 150, "");
        assertColumn(User.class, "role", "", 20, "");
    }

    private static void assertColumn(
            Class<?> entityType,
            String fieldName,
            String columnName,
            int length,
            String columnDefinition) throws NoSuchFieldException {
        Field field = entityType.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);

        assertEquals(columnName, column.name(), entityType.getSimpleName() + "." + fieldName + " column name");
        assertEquals(length, column.length(), entityType.getSimpleName() + "." + fieldName + " column length");
        assertEquals(columnDefinition, column.columnDefinition(),
                entityType.getSimpleName() + "." + fieldName + " column definition");
    }
}
