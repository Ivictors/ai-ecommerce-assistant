package com.victor.ecommerce.presentation.rest.knowledge;

import com.victor.ecommerce.application.knowledge.KnowledgeDocumentService;
import com.victor.ecommerce.domain.knowledge.KnowledgeScope;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/api/knowledge-documents")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class KnowledgeDocumentResource {
    private final KnowledgeDocumentService service;

    public KnowledgeDocumentResource(KnowledgeDocumentService service) {
        this.service = service;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@RestForm("file") FileUpload file, @RestForm("scope") String scope) {
        if (file == null || file.uploadedFile() == null) {
            throw new BadRequestException("A document file is required");
        }
        try {
            var content = java.nio.file.Files.readAllBytes(file.uploadedFile());
            if (scope == null || scope.isBlank()) {
                throw new BadRequestException("Document scope is required");
            }
            var created = service.create(file.fileName(), file.contentType(), KnowledgeScope.valueOf(scope), content);
            return Response.status(Response.Status.CREATED).entity(KnowledgeDocumentResponse.from(created)).build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Document scope is invalid");
        } catch (java.io.IOException e) {
            throw new BadRequestException("Document could not be read");
        }
    }

    @POST
    @Path("/{id}/unpublish")
    public KnowledgeDocumentResponse unpublish(@PathParam("id") Long id) {
        return KnowledgeDocumentResponse.from(service.unpublish(id));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
