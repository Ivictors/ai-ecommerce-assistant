package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.chat.AiServiceException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiServiceExceptionMapperTest {

    @Test
    void hidesProviderDetailsAndReturnsBadGateway() {
        Response response = new AiServiceExceptionMapper().toResponse(
                new AiServiceException("provider rejected key", new RuntimeException("secret"))
        );

        ApiErrorResponse error = (ApiErrorResponse) response.getEntity();
        assertEquals(502, response.getStatus());
        assertEquals("AI_PROVIDER_ERROR", error.code());
        assertEquals("The AI provider could not process the request", error.message());
    }
}
