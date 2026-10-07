package com.victor.ecommerce.application.conversation;

import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.application.security.UnauthenticatedUserException;
import com.victor.ecommerce.infrastructure.persistence.conversation.ConversationRepository;
import com.victor.ecommerce.infrastructure.persistence.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConversationServiceTest {

    @Test
    void findsConversationUsingAuthenticatedOwner() {
        ConversationRepository repository = mock(ConversationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ConversationService service = new ConversationService(repository, userRepository);
        Conversation conversation = mock(Conversation.class);
        when(repository.findByIdAndUserId(10L, 7L))
                .thenReturn(Optional.of(conversation));

        assertEquals(
                Optional.of(conversation),
                service.findByIdForUser(10L, 7L)
        );
        verify(repository).findByIdAndUserId(10L, 7L);
    }

    @Test
    void test_AC001_T001T1_createsAndPersistsConversationForVerifiedUser() {
        ConversationRepository conversationRepository = mock(ConversationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = mock(User.class);
        when(userRepository.findByIdOptional(7L)).thenReturn(Optional.of(user));
        ConversationService service = new ConversationService(conversationRepository, userRepository);

        Conversation created = service.createForUser(7L);

        assertSame(user, created.getUser());
        verify(userRepository).findByIdOptional(7L);
        verify(conversationRepository).persist(created);
    }

    @Test
    void test_AC003_T001T9_rejectsMissingPersistedUserWithoutCreatingConversation() {
        ConversationRepository conversationRepository = mock(ConversationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByIdOptional(7L)).thenReturn(Optional.empty());
        ConversationService service = new ConversationService(conversationRepository, userRepository);

        assertThrows(UnauthenticatedUserException.class, () -> service.createForUser(7L));

        verify(userRepository).findByIdOptional(7L);
        verifyNoInteractions(conversationRepository);
    }
}
