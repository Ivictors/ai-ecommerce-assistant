package com.victor.ecommerce.presentation.rest.conversation;

import com.victor.ecommerce.application.conversation.ConversationService;
import com.victor.ecommerce.application.security.CurrentUserService;
import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.domain.user.User;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationResourceTest {

    @Test
    void test_AC001_T001T2_createReturns201AndDelegatesVerifiedUserId() {
        ConversationService conversationService = mock(ConversationService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        User user = mock(User.class);
        Conversation conversation = new Conversation(user);
        when(currentUserService.getUserId()).thenReturn(42L);
        when(conversationService.createForUser(42L)).thenReturn(conversation);

        ConversationResource resource = new ConversationResource(conversationService, currentUserService);

        Response response = resource.create();

        assertEquals(201, response.getStatus());
        assertInstanceOf(ConversationResponse.class, response.getEntity());
        verify(conversationService).createForUser(42L);
    }
}
