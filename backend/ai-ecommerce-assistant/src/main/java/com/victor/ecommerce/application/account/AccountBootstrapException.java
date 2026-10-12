package com.victor.ecommerce.application.account;

import java.util.Objects;

public final class AccountBootstrapException extends RuntimeException {

    private final BootstrapFailureCode reasonCode;

    public AccountBootstrapException(BootstrapFailureCode reasonCode) {
        super(Objects.requireNonNull(reasonCode, "reasonCode").name());
        this.reasonCode = reasonCode;
    }

    public BootstrapFailureCode reasonCode() {
        return reasonCode;
    }
}
