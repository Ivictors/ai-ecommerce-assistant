package com.victor.ecommerce.infrastructure.persistence.user;

import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.domain.user.UserRole;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public boolean administratorExists() {
        return count("role", UserRole.ADMIN) > 0;
    }

    public long persistAndReturnId(User user) {
        persist(user);
        flush();
        return user.getId();
    }
}
