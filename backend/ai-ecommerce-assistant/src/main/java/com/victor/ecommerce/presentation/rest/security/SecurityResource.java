package com.victor.ecommerce.presentation.rest.security;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/api/security")
public class SecurityResource {

    @GET
    @Path("/public")
    public String publicEndpoint() {
        return "public";
    }

    @GET
    @Path("/private")
    @RolesAllowed("USER")
    public String privateEndpoint() {
        return "private";
    }
}