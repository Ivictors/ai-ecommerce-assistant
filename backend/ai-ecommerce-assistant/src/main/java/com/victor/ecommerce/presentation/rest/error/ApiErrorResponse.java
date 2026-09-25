package com.victor.ecommerce.presentation.rest.error;

public record ApiErrorResponse(
        String code,
        String message
) {
}
