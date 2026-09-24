package com.victor.ecommerce.application.chat;

import com.victor.ecommerce.application.conversation.ConversationService;
import com.victor.ecommerce.application.security.CurrentUserService;
import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.infrastructure.ai.EcommerceAssistant;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatApplicationServiceTest {

    @Test
    void rejectsConversationNotOwnedByAuthenticatedUser() {
        EcommerceAssistant assistant = mock(EcommerceAssistant.class);
        ConversationService conversations = mock(ConversationService.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        when(currentUser.getUserId()).thenReturn(7L);
        when(conversations.findByIdForUser(10L, 7L))
                .thenReturn(Optional.empty());

        ChatApplicationService service = new ChatApplicationService(
                assistant,
                conversations,
                currentUser
        );

        assertThrows(
                ConversationNotFoundException.class,
                () -> service.chat(10L, "status")
        );
        verify(conversations).findByIdForUser(10L, 7L);
    }
}
