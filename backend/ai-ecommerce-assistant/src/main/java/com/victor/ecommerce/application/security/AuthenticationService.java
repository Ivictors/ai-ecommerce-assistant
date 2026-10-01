package com.victor.ecommerce.application.security;

import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.infrastructure.persistence.user.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokenIssuer) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
    }

    @Transactional
    public AccessTokenIssuer.IssuedAccessToken authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(User::hasCredential)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return accessTokenIssuer.issue(user.getId(), user.getRole());
    }
}
