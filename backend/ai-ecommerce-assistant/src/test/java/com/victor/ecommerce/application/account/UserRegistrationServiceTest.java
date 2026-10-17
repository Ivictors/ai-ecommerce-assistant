package com.victor.ecommerce.application.account;

import com.victor.ecommerce.application.security.PasswordHasher;
import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRegistrationServiceTest {

    private final UserAccountRepository repository = mock(UserAccountRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final UserRegistrationService service = new UserRegistrationService(repository, passwordHasher);

    @Test
    void registersUserWithServerAssignedUserRoleAndHashedPassword() {
        when(repository.emailExists("user@example.com")).thenReturn(false);
        when(passwordHasher.hash("secret")).thenReturn("argon2-hash");
        when(repository.persist(any(User.class))).thenReturn(42L);

        UserRegistrationService.RegistrationResult result = service.register(
                "Test User", "user@example.com", "secret");

        assertEquals(42L, result.id());
        assertEquals("USER", result.role().name());
        verify(passwordHasher).hash("secret");
    }

    @Test
    void rejectsAnAlreadyRegisteredEmail() {
        when(repository.emailExists("user@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class,
                () -> service.register("Test User", "user@example.com", "secret"));
    }

    @Test
    void mapsAConcurrentDuplicateToThePublicRegistrationFailure() {
        when(repository.emailExists("user@example.com")).thenReturn(false);
        when(passwordHasher.hash("secret")).thenReturn("argon2-hash");
        when(repository.persist(any(User.class)))
                .thenThrow(new AccountBootstrapException(BootstrapFailureCode.DUPLICATE_EMAIL));

        assertThrows(DuplicateEmailException.class,
                () -> service.register("Test User", "user@example.com", "secret"));
    }
}
