package com.victor.ecommerce.presentation.rest.auth;

import com.victor.ecommerce.application.security.AuthenticationService;
import com.victor.ecommerce.application.account.UserRegistrationService;
import com.victor.ecommerce.presentation.rest.error.ApiErrorResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    private final AuthenticationService authenticationService;
    private final UserRegistrationService userRegistrationService;

    public AuthenticationResource(
            AuthenticationService authenticationService,
            UserRegistrationService userRegistrationService) {
        this.authenticationService = authenticationService;
        this.userRegistrationService = userRegistrationService;
    }

    @POST
    @Path("/login")
    public LoginResponse login(@Valid LoginRequest request) {
        return LoginResponse.from(authenticationService.authenticate(request.email(), request.password()));
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegistrationRequest request) {
        if (!request.password().equals(request.passwordConfirmation())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ApiErrorResponse("PASSWORD_MISMATCH", "Passwords must match"))
                    .build();
        }

        UserRegistrationService.RegistrationResult result = userRegistrationService.register(
                request.name(), request.email(), request.password());
        return Response.status(Response.Status.CREATED)
                .entity(RegistrationResponse.from(result))
                .build();
    }
}
