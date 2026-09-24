package com.victor.ecommerce.application.conversation;

import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.infrastructure.persistence.conversation.ConversationRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationServiceTest {

    @Test
    void findsConversationUsingAuthenticatedOwner() {
        ConversationRepository repository = mock(ConversationRepository.class);
        ConversationService service = new ConversationService(repository);
        Conversation conversation = mock(Conversation.class);
        when(repository.findByIdAndUserId(10L, 7L))
                .thenReturn(Optional.of(conversation));

        assertEquals(
                Optional.of(conversation),
                service.findByIdForUser(10L, 7L)
        );
        verify(repository).findByIdAndUserId(10L, 7L);
    }
}
