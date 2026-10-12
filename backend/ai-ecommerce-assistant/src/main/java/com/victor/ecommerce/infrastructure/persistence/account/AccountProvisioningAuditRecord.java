package com.victor.ecommerce.infrastructure.persistence.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "account_provisioning_audit")
public class AccountProvisioningAuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempted_at", nullable = false, insertable = false, updatable = false)
    private Instant attemptedAt;

    @Column(nullable = false, length = 10)
    private String outcome;

    @Column(name = "reason_code", nullable = false, length = 50)
    private String reasonCode;

    @Column(name = "test_user_requested", nullable = false)
    private boolean testUserRequested;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(name = "test_user_id")
    private Long testUserId;

    protected AccountProvisioningAuditRecord() {
    }

    private AccountProvisioningAuditRecord(String outcome, String reasonCode,
                                           boolean testUserRequested, Long adminUserId, Long testUserId) {
        this.outcome = outcome;
        this.reasonCode = reasonCode;
        this.testUserRequested = testUserRequested;
        this.adminUserId = adminUserId;
        this.testUserId = testUserId;
    }

    public static AccountProvisioningAuditRecord success(
            boolean testUserRequested, long adminUserId, Long testUserId) {
        return new AccountProvisioningAuditRecord(
                "SUCCESS", "SUCCESS", testUserRequested, adminUserId, testUserId);
    }

    public static AccountProvisioningAuditRecord failure(
            String reasonCode, boolean testUserRequested) {
        return new AccountProvisioningAuditRecord(
                "FAILURE", reasonCode, testUserRequested, null, null);
    }
}
