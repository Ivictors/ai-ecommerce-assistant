package com.victor.ecommerce.infrastructure.persistence.account;

import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.domain.user.UserRole;
import io.quarkus.arc.Arc;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@TestProfile(AccountBootstrapPersistenceTestProfile.class)
class AccountBootstrapPersistenceTest {

    @Inject
    DataSource dataSource;

    @BeforeEach
    void clearIsolatedApplicationRows() throws Exception {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM conversations");
            statement.executeUpdate("DELETE FROM order_items");
            statement.executeUpdate("DELETE FROM orders");
            statement.executeUpdate("DELETE FROM account_provisioning_audit");
            statement.executeUpdate("DELETE FROM users");
        }
    }

    @AfterAll
    static void cleanUpIsolatedSchema() throws Exception {
        AccountBootstrapPersistenceTestProfile.dropIsolatedSchema();
    }

    @Test
    @Tag("AC-005")
    void test_AC005_T003T1_concurrentBootstrapAttemptsAreSerialized() throws Exception {
        Object service = requiredBean("com.victor.ecommerce.application.account.AccountBootstrapService");
        Object firstRequest = request("Admin One", "admin-one@example.test", "admin-secret-one", null);
        Object secondRequest = request("Admin Two", "admin-two@example.test", "admin-secret-two", null);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Object> first = executor.submit(() -> invokeAfter(start, service, firstRequest));
            Future<Object> second = executor.submit(() -> invokeAfter(start, service, secondRequest));
            start.countDown();

            List<Object> outcomes = List.of(await(first), await(second));
            long successes = outcomes.stream().filter(AccountBootstrapPersistenceTest::isSuccessfulResult).count();
            long adminFailures = outcomes.stream().filter(AccountBootstrapPersistenceTest::isAdminAlreadyExists).count();
            assertEquals(1, successes, "exactly one concurrent bootstrap must succeed");
            assertEquals(1, adminFailures, "the serialized loser must observe the existing ADMIN: " + outcomes);
            assertEquals(1, count(dataSource, "users", "role = 'ADMIN'"), outcomes.toString());
        }
    }

    @Test
    @Tag("AC-005")
    void test_AC005_T003T1_lockIsReleasedAfterRollback() throws Exception {
        Object transaction = requiredBean("com.victor.ecommerce.infrastructure.persistence.account.PostgresAccountBootstrapTransaction");
        Object repository = requiredBean("com.victor.ecommerce.infrastructure.persistence.account.PostgresUserAccountRepository");
        Object user = new User("Rolled Back", "rolled-back@example.test", "encoded-secret", UserRole.ADMIN);

        assertThrowsInvocation(() -> invokeExecute(transaction, (Supplier<Object>) () -> {
            unchecked(() -> invoke(repository, "persist", new Class<?>[]{User.class}, user));
            throw new IllegalStateException("expected rollback");
        }));

        assertEquals(0, count(dataSource, "users", "email = 'rolled-back@example.test'"));
        assertEquals("lock-released", invokeExecute(transaction, (Supplier<Object>) () -> "lock-released"));
    }

    @Test
    @Tag("AC-011")
    void test_AC011_T003T2_successAuditCommitsWithAccounts() throws Exception {
        Object service = requiredBean("com.victor.ecommerce.application.account.AccountBootstrapService");
        Object result = invokeBootstrap(service, request("Admin", "success-admin@example.test", "admin-secret",
                new AccountInput("Test User", "success-user@example.test", "user-secret")));

        assertTrue(isSuccessfulResult(result));
        assertEquals(1, count(dataSource, "users", "email = 'success-admin@example.test'"));
        assertEquals(1, count(dataSource, "users", "email = 'success-user@example.test'"));
        assertEquals(1, count(dataSource, "account_provisioning_audit", "outcome = 'SUCCESS'"));
    }

    @Test
    @Tag("AC-011")
    void test_AC011_T003T2_successAuditFailureRollsBackAccounts() throws Exception {
        Object audit = auditWithRepositoryFailure();
        Object transaction = requiredBean("com.victor.ecommerce.application.account.AccountBootstrapTransaction");
        Object repository = requiredBean("com.victor.ecommerce.infrastructure.persistence.account.PostgresUserAccountRepository");
        Object user = new User("Audit Failure", "audit-failure@example.test", "encoded-secret", UserRole.ADMIN);

        assertThrowsInvocation(() -> invokeExecute(transaction, (Supplier<Object>) () -> {
            unchecked(() -> invoke(repository, "persist", new Class<?>[]{User.class}, user));
            unchecked(() -> invoke(audit, "recordSuccess", new Class<?>[]{boolean.class, long.class, Long.class}, false, 1L, null));
            return null;
        }));

        assertEquals(0, count(dataSource, "users", "email = 'audit-failure@example.test'"));
        assertEquals(0, count(dataSource, "account_provisioning_audit", "outcome = 'SUCCESS'"));
    }

    @Test
    @Tag("AC-011")
    void test_AC011_T003T3_failureAuditSurvivesAccountRollback() throws Exception {
        Object transaction = requiredBean("com.victor.ecommerce.application.account.AccountBootstrapTransaction");
        Object repository = requiredBean("com.victor.ecommerce.infrastructure.persistence.account.PostgresUserAccountRepository");
        Object audit = requiredBean("com.victor.ecommerce.application.account.AccountProvisioningAudit");
        Object user = new User("Failed Account", "failed-account@example.test", "encoded-secret", UserRole.ADMIN);

        assertThrowsInvocation(() -> invokeExecute(transaction, (Supplier<Object>) () -> {
            unchecked(() -> invoke(repository, "persist", new Class<?>[]{User.class}, user));
            throw new IllegalStateException("expected bootstrap failure");
        }));
        invoke(audit, "recordFailure",
                new Class<?>[]{requiredType("com.victor.ecommerce.application.account.BootstrapFailureCode"), boolean.class},
                enumValue("BOOTSTRAP_FAILURE"), false);

        assertEquals(0, count(dataSource, "users", "email = 'failed-account@example.test'"));
        assertEquals(1, count(dataSource, "account_provisioning_audit",
                "outcome = 'FAILURE' AND reason_code = 'BOOTSTRAP_FAILURE'"));
    }

    @Test
    @Tag("AC-011")
    void test_AC011_T003T4_auditFailureLogsOnlySafeFields() throws Exception {
        Object audit = auditWithRepositoryFailure();
        Logger logger = Logger.getLogger("com.victor.ecommerce.infrastructure.persistence.account.PostgresAccountProvisioningAudit");
        CapturedLogs captured = new CapturedLogs();
        logger.addHandler(captured);
        logger.setLevel(Level.ALL);
        try {
            invoke(audit, "recordFailure",
                    new Class<?>[]{requiredType("com.victor.ecommerce.application.account.BootstrapFailureCode"), boolean.class},
                    enumValue("BOOTSTRAP_FAILURE"), true);
        } finally {
            logger.removeHandler(captured);
        }

        String output = captured.messages();
        assertTrue(output.contains("BOOTSTRAP_FAILURE"));
        assertFalse(output.contains("sentinel-password"));
        assertFalse(output.contains("sentinel-hash"));
        assertFalse(output.contains("operator@example.test"));
        assertFalse(output.contains("simulated database exception"));
    }

    @Test
    @Tag("AC-011")
    void test_AC011_T003T5_auditRowsContainOnlyApprovedColumnsAndNoSecrets() throws Exception {
        Object service = requiredBean("com.victor.ecommerce.application.account.AccountBootstrapService");
        invokeBootstrap(service, request("Safe Admin", "safe-admin@example.test", "sentinel-password",
                new AccountInput("Safe User", "safe-user@example.test", "sentinel-user-password")));

        Set<String> columns = columns(dataSource, "account_provisioning_audit");
        assertEquals(Set.of("id", "attempted_at", "outcome", "reason_code", "test_user_requested",
                "admin_user_id", "test_user_id"), columns);
        assertEquals(0, count(dataSource, "account_provisioning_audit", "reason_code LIKE '%sentinel%'"));
        assertEquals(0, count(dataSource, "account_provisioning_audit", "reason_code LIKE '%@%'"));
        assertEquals(0, count(dataSource, "account_provisioning_audit", "reason_code LIKE '%password%'"));
    }

    @Test
    @Tag("AC-010")
    void test_AC010_T003T6_uniqueEmailConflictIsSafeAndAtomic() throws Exception {
        Object service = requiredBean("com.victor.ecommerce.application.account.AccountBootstrapService");
        invokeBootstrap(service, request("Existing Admin", "duplicate@example.test", "first-secret", null));

        Throwable failure = assertThrowsInvocation(() -> invokeBootstrap(service,
                request("Second Admin", "duplicate@example.test", "second-secret", null)));
        assertTrue(failure.toString().contains("DUPLICATE_EMAIL")
                || failure.toString().contains("ADMIN_ALREADY_EXISTS"));
        assertEquals(1, count(dataSource, "users", "email = 'duplicate@example.test'"));
        assertFalse(failure.toString().contains("second-secret"));
    }

    private Object auditWithRepositoryFailure() throws Exception {
        Class<?> auditType = requiredType("com.victor.ecommerce.infrastructure.persistence.account.PostgresAccountProvisioningAudit");
        Class<?> repositoryType = requiredType("com.victor.ecommerce.infrastructure.persistence.account.AccountProvisioningAuditRepository");
        AccountProvisioningAuditRepository repository = mock(AccountProvisioningAuditRepository.class);
        doThrow(new IllegalStateException(
                "simulated database exception: sentinel-password sentinel-hash operator@example.test"))
                .when(repository).persistAndFlush(any(AccountProvisioningAuditRecord.class));
        var constructor = Arrays.stream(auditType.getConstructors())
                .filter(candidate -> candidate.getParameterCount() == 1
                        && candidate.getParameterTypes()[0].isAssignableFrom(repositoryType))
                .findFirst()
                .orElseGet(() -> {
                    fail("T-003 requires an injectable audit-repository constructor");
                    return null;
                });
        return constructor.newInstance(repository);
    }

    private static Object invokeBootstrap(Object service, Object request) throws Exception {
        return invoke(service, "bootstrap", new Class<?>[]{request.getClass()}, request);
    }

    private static Object request(String adminName, String adminEmail, String adminPassword, AccountInput testUser)
            throws Exception {
        Class<?> detailsType = requiredType("com.victor.ecommerce.application.account.AccountBootstrapService$AccountDetails");
        Class<?> requestType = requiredType("com.victor.ecommerce.application.account.AccountBootstrapService$BootstrapRequest");
        Object admin = detailsType.getConstructor(String.class, String.class, String.class)
                .newInstance(adminName, adminEmail, adminPassword);
        Optional<?> optionalUser = testUser == null
                ? Optional.empty()
                : Optional.of(detailsType.getConstructor(String.class, String.class, String.class)
                        .newInstance(testUser.name(), testUser.email(), testUser.password()));
        return requestType.getConstructor(detailsType, Optional.class).newInstance(admin, optionalUser);
    }

    private static Object invokeAfter(CountDownLatch start, Object service, Object request) throws Exception {
        start.await(10, TimeUnit.SECONDS);
        return invoke(service, "bootstrap", new Class<?>[]{request.getClass()}, request);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        try {
            return target.getClass().getMethod(methodName, parameterTypes).invoke(target, args);
        } catch (InvocationTargetException failure) {
            throw failure;
        }
    }

    private static Object invokeExecute(Object transaction, Supplier<Object> work) throws Exception {
        return invoke(transaction, "execute", new Class<?>[]{Supplier.class}, work);
    }

    private static <T> T unchecked(Callable<T> operation) {
        try {
            return operation.call();
        } catch (Exception failure) {
            return sneakyThrow(failure);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T, E extends Throwable> T sneakyThrow(Throwable failure) throws E {
        throw (E) failure;
    }

    private static Object requiredBean(String className) throws Exception {
        Class<?> type = requiredType(className);
        var instance = Arc.container().instance(type);
        try {
            Object bean = instance.get();
            assertTrue(bean != null, "T-003 requires a resolvable CDI adapter: " + className);
            return bean;
        } catch (RuntimeException missingBean) {
            fail("T-003 requires a resolvable CDI adapter: " + className);
            return null;
        }
    }

    private static Class<?> requiredType(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException missingType) {
            fail("T-003 requires production type " + name);
            return null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object enumValue(String name) {
        return Enum.valueOf((Class) requiredType("com.victor.ecommerce.application.account.BootstrapFailureCode"), name);
    }

    private static Object await(Future<Object> future) throws Exception {
        try {
            return future.get(15, TimeUnit.SECONDS);
        } catch (ExecutionException failure) {
            Throwable cause = failure.getCause();
            return cause instanceof InvocationTargetException invocation
                    ? invocation.getTargetException()
                    : cause;
        }
    }

    private static boolean isSuccessfulResult(Object outcome) {
        return outcome != null && !(outcome instanceof Throwable);
    }

    private static boolean isAdminAlreadyExists(Object outcome) {
        return outcome instanceof Throwable && outcome.toString().contains("ADMIN_ALREADY_EXISTS");
    }

    private static Throwable assertThrowsInvocation(Callable<?> operation) throws Exception {
        try {
            operation.call();
            fail("T-003 expected the transaction/audit operation to fail");
            return null;
        } catch (InvocationTargetException failure) {
            return failure.getTargetException();
        } catch (RuntimeException failure) {
            return failure;
        }
    }

    private static int count(DataSource dataSource, String table, String predicate) throws Exception {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE " + predicate)) {
            result.next();
            return result.getInt(1);
        }
    }

    private static Set<String> columns(DataSource dataSource, String table) throws Exception {
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(
                "SELECT column_name FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = ?")) {
            statement.setString(1, table);
            try (var result = statement.executeQuery()) {
                Set<String> columns = new java.util.HashSet<>();
                while (result.next()) {
                    columns.add(result.getString(1));
                }
                return columns;
            }
        }
    }

    private record AccountInput(String name, String email, String password) {
    }

    private static final class CapturedLogs extends Handler {
        private final StringBuilder messages = new StringBuilder();

        @Override
        public void publish(LogRecord record) {
            messages.append(record.getMessage()).append('\n');
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        private String messages() {
            return messages.toString();
        }
    }
}
