package com.victor.ecommerce.application.security;

import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.domain.user.UserRole;
import com.victor.ecommerce.infrastructure.persistence.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final AccessTokenIssuer tokenIssuer = mock(AccessTokenIssuer.class);
    private final AuthenticationService service = new AuthenticationService(
            userRepository,
            passwordHasher,
            tokenIssuer
    );

    @Test
    void validCredentialsMustIssueToken() throws Exception {
        User user = userWithCredential("stored-hash");
        AccessTokenIssuer.IssuedAccessToken expected = new AccessTokenIssuer.IssuedAccessToken("token", 900);

        when(userRepository.findByEmail("victor@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("secret", "stored-hash")).thenReturn(true);
        when(tokenIssuer.issue(1L, UserRole.USER)).thenReturn(expected);

        assertEquals(expected, service.authenticate("victor@example.com", "secret"));
        verify(tokenIssuer).issue(1L, UserRole.USER);
    }

    @Test
    void invalidPasswordMustBeRejected() throws Exception {
        User user = userWithCredential("stored-hash");
        when(userRepository.findByEmail("victor@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "stored-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> service.authenticate("victor@example.com", "wrong"));
    }

    @Test
    void userWithoutCredentialMustBeRejected() throws Exception {
        User user = userWithoutCredential();
        when(userRepository.findByEmail("victor@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class,
                () -> service.authenticate("victor@example.com", "secret"));
    }

    private User userWithCredential(String passwordHash) throws Exception {
        Constructor<User> constructor;
        try {
            constructor = User.class.getConstructor(String.class, String.class, String.class, UserRole.class);
        } catch (NoSuchMethodException missingApprovedConstructor) {
            constructor = null;
        }
        assertNotNull(constructor, "T-002 requires the credentialed User constructor");
        User user = constructor.newInstance("Victor", "victor@example.com", passwordHash, UserRole.USER);
        setField(user, "id", 1L);
        return user;
    }

    private User userWithoutCredential() throws Exception {
        Constructor<User> constructor = User.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        User user = constructor.newInstance();
        setField(user, "id", 1L);
        setField(user, "name", "Victor");
        setField(user, "email", "victor@example.com");
        setField(user, "role", UserRole.USER);
        return user;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
