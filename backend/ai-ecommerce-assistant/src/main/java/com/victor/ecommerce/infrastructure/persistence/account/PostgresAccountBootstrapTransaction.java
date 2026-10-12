package com.victor.ecommerce.infrastructure.persistence.account;

import com.victor.ecommerce.application.account.AccountBootstrapTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import io.quarkus.arc.Unremovable;

import java.util.function.Supplier;

@ApplicationScoped
@Unremovable
public class PostgresAccountBootstrapTransaction implements AccountBootstrapTransaction {

    private static final long BOOTSTRAP_LOCK_KEY = 8_174_291_503L;

    private final EntityManager entityManager;

    public PostgresAccountBootstrapTransaction(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public <T> T execute(Supplier<T> work) {
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:lockKey)")
                .setParameter("lockKey", BOOTSTRAP_LOCK_KEY)
                .getSingleResult();
        return work.get();
    }
}
