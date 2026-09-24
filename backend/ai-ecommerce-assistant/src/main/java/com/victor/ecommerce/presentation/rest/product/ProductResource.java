package com.victor.ecommerce.presentation.rest.product;

import com.victor.ecommerce.application.product.ProductService;
import com.victor.ecommerce.domain.product.Product;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductService productService;

    public ProductResource(ProductService productService) {
        this.productService = productService;
    }

    @GET
    public List<ProductResponse> findAll() {
        return productService.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {

        Product product = productService.findById(id);

        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        return Response.ok(ProductResponse.from(product))
                .build();
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid CreateProductRequest request) {

        Product product = productService.create(
                request.name(),
                request.description(),
                request.price(),
                request.stock()
        );

        return Response
                .created(URI.create("/api/products/" + product.getId()))
                .entity(ProductResponse.from(product))
                .build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {

        boolean deleted = productService.delete(id);

        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        return Response.noContent().build();
    }
}
