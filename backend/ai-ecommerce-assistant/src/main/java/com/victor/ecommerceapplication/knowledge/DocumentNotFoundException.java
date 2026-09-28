package com.victor.ecommerce.application.knowledge;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException() { super("Document not found"); }
}
