package com.victor.ecommerce.presentation.rest.auth;

import com.victor.ecommerce.application.security.AccessTokenIssuer;
import com.victor.ecommerce.application.security.AuthenticationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationResourceTest {

    private final AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final AuthenticationResource resource = new AuthenticationResource(authenticationService);

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
}
