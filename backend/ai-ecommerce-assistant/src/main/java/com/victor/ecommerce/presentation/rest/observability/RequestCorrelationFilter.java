package com.victor.ecommerce.presentation.rest.observability;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Propagates a bounded request identifier without copying arbitrary client input
 * into logs or response headers.
 */
@Provider
public class RequestCorrelationFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String HEADER_NAME = "X-Request-ID";
    private static final String PROPERTY_NAME = RequestCorrelationFilter.class.getName() + ".id";
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,100}");

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String suppliedId = requestContext.getHeaderString(HEADER_NAME);
        String requestId = suppliedId != null && SAFE_REQUEST_ID.matcher(suppliedId).matches()
                ? suppliedId
                : UUID.randomUUID().toString();
        requestContext.setProperty(PROPERTY_NAME, requestId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        Object requestId = requestContext.getProperty(PROPERTY_NAME);
        if (requestId instanceof String value) {
            responseContext.getHeaders().putSingle(HEADER_NAME, value);
        }
    }

    public static String requestId(ContainerRequestContext requestContext) {
        if (requestContext == null) {
            return "unavailable";
        }
        Object requestId = requestContext.getProperty(PROPERTY_NAME);
        return requestId instanceof String value ? value : "unavailable";
    }
}
