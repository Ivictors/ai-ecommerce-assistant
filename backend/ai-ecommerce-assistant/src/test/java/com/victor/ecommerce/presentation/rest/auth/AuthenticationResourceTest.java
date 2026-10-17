package com.victor.ecommerce.presentation.rest.auth;

import com.victor.ecommerce.application.security.AccessTokenIssuer;
import com.victor.ecommerce.application.security.AuthenticationService;
import com.victor.ecommerce.application.account.UserRegistrationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationResourceTest {

    private final AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final UserRegistrationService userRegistrationService = mock(UserRegistrationService.class);
    private final AuthenticationResource resource = new AuthenticationResource(
            authenticationService,
            userRegistrationService);

    @Test
    void loginMustReturnBearerTokenAndExpiration() {
        LoginRequest request = new LoginRequest("victor@example.com", "secret");
        AccessTokenIssuer.IssuedAccessToken issuedToken =
                new AccessTokenIssuer.IssuedAccessToken("signed-token", 900);
        when(authenticationService.authenticate(request.email(), request.password()))
                .thenReturn(issuedToken);

        LoginResponse response = resource.login(request);

        assertEquals("signed-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900, response.expiresIn());
    }

    @Test
    void registerReturnsCreatedUserWithoutCredentials() {
        RegistrationRequest request = new RegistrationRequest(
                "New User", "new@example.com", "secret", "secret");
        when(userRegistrationService.register(request.name(), request.email(), request.password()))
                .thenReturn(new UserRegistrationService.RegistrationResult(
                        7L, request.name(), request.email(), com.victor.ecommerce.domain.user.UserRole.USER));

        var response = resource.register(request);

        assertEquals(201, response.getStatus());
        RegistrationResponse body = (RegistrationResponse) response.getEntity();
        assertEquals(7L, body.id());
        assertEquals("USER", body.role());
        assertTrue(!body.toString().contains("secret"));
    }

    @Test
    void registerRejectsDifferentPasswords() {
        var response = resource.register(new RegistrationRequest(
                "New User", "new@example.com", "secret", "different"));

        assertEquals(400, response.getStatus());
    }
}
