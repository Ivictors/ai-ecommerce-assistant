package com.victor.ecommerce.application.account;

import java.util.function.Supplier;

public interface AccountBootstrapTransaction {

    <T> T execute(Supplier<T> work);
}
