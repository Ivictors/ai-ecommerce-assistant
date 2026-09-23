package com.victor.ecommerce.application.security;

public class UnauthenticatedUserException extends IllegalStateException {

    public UnauthenticatedUserException() {
        super("Authenticated user identity is unavailable");
    }
}
