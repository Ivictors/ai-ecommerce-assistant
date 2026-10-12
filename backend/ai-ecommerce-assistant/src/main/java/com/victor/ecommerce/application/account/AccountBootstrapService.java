package com.victor.ecommerce.application.account;

import com.victor.ecommerce.application.security.PasswordHasher;
import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.domain.user.UserRole;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.Unremovable;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.regex.Pattern;

@ApplicationScoped
@Unremovable
public class AccountBootstrapService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final AccountBootstrapTransaction transaction;
    private final UserAccountRepository userAccountRepository;
    private final AccountProvisioningAudit audit;
    private final PasswordHasher passwordHasher;

    public AccountBootstrapService(
            AccountBootstrapTransaction transaction,
            UserAccountRepository userAccountRepository,
            AccountProvisioningAudit audit,
            PasswordHasher passwordHasher) {
        this.transaction = transaction;
        this.userAccountRepository = userAccountRepository;
        this.audit = audit;
        this.passwordHasher = passwordHasher;
    }

    public BootstrapResult bootstrap(BootstrapRequest request) {
        boolean testUserRequested = request != null
                && request.testUser() != null
                && request.testUser().isPresent();

        try {
            validate(request);
            return transaction.execute(() -> createAccounts(request, testUserRequested));
        } catch (AccountBootstrapException failure) {
            recordFailure(failure.reasonCode(), testUserRequested);
            throw failure;
        } catch (RuntimeException failure) {
            AccountBootstrapException safeFailure = new AccountBootstrapException(
                    BootstrapFailureCode.BOOTSTRAP_FAILURE);
            recordFailure(safeFailure.reasonCode(), testUserRequested);
            throw safeFailure;
        }
    }

    private BootstrapResult createAccounts(BootstrapRequest request, boolean testUserRequested) {
        if (userAccountRepository.administratorExists()) {
            throw new AccountBootstrapException(BootstrapFailureCode.ADMIN_ALREADY_EXISTS);
        }

        AccountDetails admin = request.admin();
        rejectExistingEmail(admin.email());
        request.testUser().ifPresent(details -> rejectExistingEmail(details.email()));

        String adminHash = passwordHasher.hash(admin.password());
        String testUserHash = request.testUser()
                .map(details -> passwordHasher.hash(details.password()))
                .orElse(null);

        long adminId = userAccountRepository.persist(
                new User(admin.name(), admin.email(), adminHash, UserRole.ADMIN));
        Long testUserId = request.testUser()
                .map(details -> userAccountRepository.persist(
                        new User(details.name(), details.email(), testUserHash, UserRole.USER)))
                .orElse(null);

        try {
            audit.recordSuccess(testUserRequested, adminId, testUserId);
        } catch (RuntimeException auditFailure) {
            throw new AccountBootstrapException(BootstrapFailureCode.AUDIT_FAILURE);
        }

        return new BootstrapResult(adminId,
                testUserId == null ? OptionalLong.empty() : OptionalLong.of(testUserId));
    }

    private void rejectExistingEmail(String email) {
        if (userAccountRepository.emailExists(email)) {
            throw new AccountBootstrapException(BootstrapFailureCode.DUPLICATE_EMAIL);
        }
    }

    private void recordFailure(BootstrapFailureCode reason, boolean testUserRequested) {
        try {
            audit.recordFailure(reason, testUserRequested);
        } catch (RuntimeException ignored) {
            // The audit adapter owns the safe logging fallback when persistence is unavailable.
        }
    }

    private static void validate(BootstrapRequest request) {
        if (request == null || request.admin() == null || request.testUser() == null) {
            throw new AccountBootstrapException(BootstrapFailureCode.INVALID_INPUT);
        }

        validateDetails(request.admin());
        request.testUser().ifPresent(AccountBootstrapService::validateDetails);

        if (request.testUser().isPresent()
                && request.admin().email().equals(request.testUser().orElseThrow().email())) {
            throw new AccountBootstrapException(BootstrapFailureCode.DUPLICATE_EMAIL);
        }
    }

    private static void validateDetails(AccountDetails details) {
        if (details == null
                || blank(details.name())
                || details.name().length() > 150
                || blank(details.email())
                || details.email().length() > 255
                || !EMAIL_PATTERN.matcher(details.email()).matches()
                || blank(details.password())) {
            throw new AccountBootstrapException(BootstrapFailureCode.INVALID_INPUT);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record AccountDetails(String name, String email, String password) {

        @Override
        public String toString() {
            return "AccountDetails[name=" + name + ", email=" + email + ", password=[REDACTED]]";
        }
    }

    public record BootstrapRequest(AccountDetails admin, Optional<AccountDetails> testUser) {
    }

    public record BootstrapResult(long adminUserId, OptionalLong testUserId) {
    }
}
