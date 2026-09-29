package com.victor.ecommerce.presentation.rest.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatResponseTest {
    @Test
    void textResponseHasEnvelopeAndNoBusinessData() {
        var response = ChatResponse.text("Hello");
        assertEquals(ChatResponseType.TEXT, response.type());
        assertEquals("1", response.schemaVersion());
        assertNull(response.data());
    }

    @Test
    void policyResponseHasTypedData() {
        var response = ChatResponse.policy("Policy", new PolicyInformationData("Seven days"));
        assertEquals(ChatResponseType.POLICY_INFORMATION, response.type());
        assertEquals("Seven days", ((PolicyInformationData) response.data()).content());
    }
}
