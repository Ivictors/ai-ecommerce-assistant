package com.victor.ecommerce.presentation.rest.observability;

import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @Test
    void preservesSafeClientRequestIdAndReturnsIt() throws Exception {
        var request = mock(jakarta.ws.rs.container.ContainerRequestContext.class);
        var response = mock(jakarta.ws.rs.container.ContainerResponseContext.class);
        var properties = new HashMap<String, Object>();
        var headers = new MultivaluedHashMap<String, Object>();

        when(request.getHeaderString(RequestCorrelationFilter.HEADER_NAME)).thenReturn("checkout-42");
        when(request.getPropertyNames()).thenReturn(properties.keySet());
        when(request.getProperty(org.mockito.ArgumentMatchers.anyString())).thenAnswer(invocation ->
                properties.get(invocation.getArgument(0)));
        when(response.getHeaders()).thenReturn(headers);

        filter.filter(request);
        org.mockito.Mockito.verify(request).setProperty(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq("checkout-42"));
        properties.put(RequestCorrelationFilter.class.getName() + ".id", "checkout-42");
        filter.filter(request, response);

        assertEquals("checkout-42", headers.getFirst(RequestCorrelationFilter.HEADER_NAME));
    }

    @Test
    void replacesUnsafeClientRequestIdWithGeneratedIdentifier() throws Exception {
        var request = mock(jakarta.ws.rs.container.ContainerRequestContext.class);
        Map<String, Object> properties = new HashMap<>();

        when(request.getHeaderString(RequestCorrelationFilter.HEADER_NAME)).thenReturn("token\r\nforged-log-entry");

        filter.filter(request);

        var captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        org.mockito.Mockito.verify(request).setProperty(org.mockito.ArgumentMatchers.anyString(), captor.capture());
        String requestId = (String) captor.getValue();
        assertNotNull(requestId);
        assertTrue(requestId.matches("[0-9a-f-]{36}"));
        properties.put(RequestCorrelationFilter.class.getName() + ".id", requestId);
    }
}
