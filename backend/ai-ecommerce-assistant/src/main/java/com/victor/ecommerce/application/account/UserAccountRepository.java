package com.victor.ecommerce.application.account;

import com.victor.ecommerce.domain.user.User;

public interface UserAccountRepository {

    boolean administratorExists();

    boolean emailExists(String email);

    long persist(User user);
}
