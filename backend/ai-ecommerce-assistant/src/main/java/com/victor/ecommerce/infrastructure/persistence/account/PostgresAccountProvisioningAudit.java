package com.victor.ecommerce.infrastructure.persistence.account;

import com.victor.ecommerce.application.account.AccountProvisioningAudit;
import com.victor.ecommerce.application.account.BootstrapFailureCode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import io.quarkus.arc.Unremovable;
import org.jboss.logging.Logger;

@ApplicationScoped
@Unremovable
public class PostgresAccountProvisioningAudit implements AccountProvisioningAudit {

    private static final Logger LOG = Logger.getLogger(PostgresAccountProvisioningAudit.class);

    private final AccountProvisioningAuditRepository repository;

    public PostgresAccountProvisioningAudit(AccountProvisioningAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    public void recordSuccess(boolean testUserRequested, long adminUserId, Long testUserId) {
        repository.persistAndFlush(
                AccountProvisioningAuditRecord.success(testUserRequested, adminUserId, testUserId));
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void recordFailure(BootstrapFailureCode reason, boolean testUserRequested) {
        try {
            repository.persistAndFlush(
                    AccountProvisioningAuditRecord.failure(reason.name(), testUserRequested));
        } catch (RuntimeException failure) {
            LOG.errorf("account_provisioning_audit_failed outcome=FAILURE reason=%s", reason.name());
        }
    }
}
