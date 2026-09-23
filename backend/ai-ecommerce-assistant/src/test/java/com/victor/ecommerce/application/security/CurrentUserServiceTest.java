package com.victor.ecommerce.application.security;

import io.quarkus.security.identity.SecurityIdentity;
import org.junit.jupiter.api.Test;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserServiceTest {

    @Test
    void returnsAuthenticatedUserIdFromPrincipal() {
        SecurityIdentity identity = mock(SecurityIdentity.class);
        Principal principal = mock(Principal.class);
        when(identity.isAnonymous()).thenReturn(false);
        when(identity.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("42");

        assertEquals(42L, new CurrentUserService(identity).getUserId());
    }

    @Test
    void rejectsAnonymousUser() {
        SecurityIdentity identity = mock(SecurityIdentity.class);
        when(identity.isAnonymous()).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> new CurrentUserService(identity).getUserId());
    }

    @Test
    void rejectsAuthenticatedIdentityWithoutPrincipal() {
        SecurityIdentity identity = mock(SecurityIdentity.class);
        when(identity.isAnonymous()).thenReturn(false);
        when(identity.getPrincipal()).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> new CurrentUserService(identity).getUserId());
    }

    @Test
    void rejectsNonNumericPrincipal() {
        SecurityIdentity identity = mock(SecurityIdentity.class);
        Principal principal = mock(Principal.class);
        when(identity.isAnonymous()).thenReturn(false);
        when(identity.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("not-a-number");

        assertThrows(IllegalStateException.class,
                () -> new CurrentUserService(identity).getUserId());
    }

    @Test
    void rejectsEmptyPrincipal() {
        SecurityIdentity identity = mock(SecurityIdentity.class);
        Principal principal = mock(Principal.class);
        when(identity.isAnonymous()).thenReturn(false);
        when(identity.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("   ");

        assertThrows(IllegalStateException.class,
                () -> new CurrentUserService(identity).getUserId());
    }
}
