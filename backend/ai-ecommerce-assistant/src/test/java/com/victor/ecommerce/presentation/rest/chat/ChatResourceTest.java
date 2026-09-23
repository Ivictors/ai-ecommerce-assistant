package com.victor.ecommerce.presentation.rest.chat;

import com.victor.ecommerce.application.chat.ChatApplicationService;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ChatResourceTest {

    @Test
    void rejectsMissingConversationId() {
        ChatApplicationService chatService = mock(ChatApplicationService.class);
        ChatResource resource = new ChatResource(chatService);

        assertThrows(
                BadRequestException.class,
                () -> resource.chat(null, "What is the status of my order?")
        );

        verifyNoInteractions(chatService);
    }
}
