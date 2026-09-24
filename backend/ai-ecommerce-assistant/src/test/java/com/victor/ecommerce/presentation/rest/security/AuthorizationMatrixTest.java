package com.victor.ecommerce.presentation.rest.security;

import com.victor.ecommerce.presentation.rest.chat.ChatResource;
import com.victor.ecommerce.presentation.rest.conversation.ConversationResource;
import com.victor.ecommerce.presentation.rest.order.OrderResource;
import com.victor.ecommerce.presentation.rest.product.ProductResource;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthorizationMatrixTest {

    @Test
    void protectsChatForUsers() {
        assertRoles(ChatResource.class, "USER");
    }

    @Test
    void protectsConversationsForUsers() {
        assertRoles(ConversationResource.class, "USER");
    }

    @Test
    void protectsOrdersForUsers() {
        assertRoles(OrderResource.class, "USER");
    }

    @Test
    void protectsProductMutationsForAdministrators() {
        assertMethodRoles(ProductResource.class, "create", "ADMIN");
        assertMethodRoles(ProductResource.class, "delete", "ADMIN");
    }

    private void assertRoles(Class<?> resourceType, String... expectedRoles) {
        RolesAllowed rolesAllowed = resourceType.getAnnotation(RolesAllowed.class);

        assertNotNull(rolesAllowed, resourceType.getSimpleName());
        assertArrayEquals(expectedRoles, rolesAllowed.value());
    }

    private void assertMethodRoles(
            Class<?> resourceType,
            String methodName,
            String... expectedRoles) {

        Method method = java.util.Arrays.stream(resourceType.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();

        RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);

        assertNotNull(rolesAllowed, methodName);
        assertArrayEquals(expectedRoles, rolesAllowed.value());
    }
}
