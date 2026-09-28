package com.victor.ecommerce.infrastructure.knowledge;

import com.victor.ecommerce.application.knowledge.DocumentTextExtractor;
import com.victor.ecommerce.application.knowledge.InvalidDocumentException;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class BasicDocumentTextExtractor implements DocumentTextExtractor {
    @Override
    public String extract(String filename, String mediaType, byte[] content) {
        String text;
        if (mediaType != null && mediaType.equalsIgnoreCase("application/pdf")) {
            try (var pdf = Loader.loadPDF(content)) {
                text = new PDFTextStripper().getText(pdf).trim();
            } catch (Exception exception) {
                throw new InvalidDocumentException("Document PDF is invalid");
            }
        } else {
            text = new String(content, StandardCharsets.UTF_8).trim();
        }
        if (text.isBlank()) {
            throw new InvalidDocumentException("Document contains no usable text");
        }
        return text;
    }
}
