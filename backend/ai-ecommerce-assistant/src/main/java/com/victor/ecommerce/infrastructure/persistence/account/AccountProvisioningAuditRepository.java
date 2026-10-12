package com.victor.ecommerce.infrastructure.persistence.account;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.Unremovable;

@ApplicationScoped
@Unremovable
public class AccountProvisioningAuditRepository implements PanacheRepository<AccountProvisioningAuditRecord> {

    public void persistAndFlush(AccountProvisioningAuditRecord record) {
        persist(record);
        flush();
    }
}
