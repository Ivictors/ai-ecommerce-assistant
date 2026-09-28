package com.victor.ecommerce.application.chat;

import com.victor.ecommerce.application.conversation.ConversationService;
import com.victor.ecommerce.application.security.CurrentUserService;
import com.victor.ecommerce.application.knowledge.KnowledgeRetriever;
import com.victor.ecommerce.application.knowledge.RetrievedKnowledge;
import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.infrastructure.ai.EcommerceAssistant;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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

    @Test
    void sendsRetrievedKnowledgeContextToAssistant() {
        EcommerceAssistant assistant = mock(EcommerceAssistant.class);
        ConversationService conversations = mock(ConversationService.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        KnowledgeRetriever retriever = mock(KnowledgeRetriever.class);
        Conversation conversation = mock(Conversation.class);
        when(conversation.getId()).thenReturn(10L);
        when(currentUser.getUserId()).thenReturn(7L);
        when(conversations.findByIdForUser(10L, 7L)).thenReturn(Optional.of(conversation));
        when(retriever.retrieve("return policy")).thenReturn(List.of(new RetrievedKnowledge(1L, 1L, 0, "Seven days")));
        when(assistant.chat(any(), any())).thenReturn("answer");

        ChatApplicationService service = new ChatApplicationService(assistant, conversations, currentUser, retriever);

        service.chat(10L, "return policy");

        verify(assistant).chat(eq("10"), contains("Seven days"));
    }
}
