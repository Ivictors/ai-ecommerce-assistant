package com.victor.ecommerce.application.account;

public interface AccountProvisioningAudit {

    void recordSuccess(boolean testUserRequested, long adminUserId, Long testUserId);

    void recordFailure(BootstrapFailureCode reason, boolean testUserRequested);
}
