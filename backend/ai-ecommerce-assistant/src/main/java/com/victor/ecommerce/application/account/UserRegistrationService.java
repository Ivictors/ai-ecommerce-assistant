package com.victor.ecommerce.application.account;

import com.victor.ecommerce.application.security.PasswordHasher;
import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.domain.user.UserRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.regex.Pattern;

@ApplicationScoped
public class UserRegistrationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;

    public UserRegistrationService(
            UserAccountRepository userAccountRepository,
            PasswordHasher passwordHasher) {
        this.userAccountRepository = userAccountRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public RegistrationResult register(String name, String email, String password) {
        if (blank(name) || blank(email) || blank(password) || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid registration data");
        }
        if (userAccountRepository.emailExists(email)) {
            throw new DuplicateEmailException();
        }

        User user = new User(name, email, passwordHasher.hash(password), UserRole.USER);
        try {
            long userId = userAccountRepository.persist(user);
            return new RegistrationResult(userId, user.getName(), user.getEmail(), user.getRole());
        } catch (AccountBootstrapException duplicate) {
            throw new DuplicateEmailException();
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record RegistrationResult(long id, String name, String email, UserRole role) {
    }
}
