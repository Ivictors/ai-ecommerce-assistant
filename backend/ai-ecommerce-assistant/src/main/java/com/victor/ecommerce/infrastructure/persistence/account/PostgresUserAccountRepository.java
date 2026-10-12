package com.victor.ecommerce.infrastructure.persistence.account;

import com.victor.ecommerce.application.account.AccountBootstrapException;
import com.victor.ecommerce.application.account.BootstrapFailureCode;
import com.victor.ecommerce.application.account.UserAccountRepository;
import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.infrastructure.persistence.user.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.Unremovable;
import org.hibernate.exception.ConstraintViolationException;

@ApplicationScoped
@Unremovable
public class PostgresUserAccountRepository implements UserAccountRepository {

    private final UserRepository userRepository;

    public PostgresUserAccountRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean administratorExists() {
        return userRepository.administratorExists();
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public long persist(User user) {
        try {
            return userRepository.persistAndReturnId(user);
        } catch (RuntimeException failure) {
            if (isUniqueConstraintViolation(failure)) {
                throw new AccountBootstrapException(BootstrapFailureCode.DUPLICATE_EMAIL);
            }
            throw failure;
        }
    }

    private static boolean isUniqueConstraintViolation(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null
                    && violation.getConstraintName().contains("email")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
