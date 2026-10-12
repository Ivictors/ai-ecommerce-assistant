package com.victor.ecommerce.application.account;

import com.victor.ecommerce.application.security.PasswordHasher;
import com.victor.ecommerce.domain.user.UserRole;
import com.victor.ecommerce.infrastructure.security.Argon2PasswordHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountBootstrapServiceTest {

    @Test
    @Tag("AC-001")
    @Tag("AC-003")
    @DisplayName("T-002-T1: hashes and creates only the first ADMIN")
    void test_AC001_AC003_T002T1_bootstrapsAdminOnly() throws Exception {
        Fixture fixture = new Fixture();

        Object result = fixture.bootstrap(details("Admin", "admin@example.com", "admin-secret"), Optional.empty());

        assertEquals(1, fixture.repository.committed.size());
        assertEquals(UserRole.ADMIN, fixture.repository.committed.get(0).role());
        assertEquals("encoded:admin-secret", fixture.repository.committed.get(0).passwordHash());
        assertEquals(List.of("admin-secret"), fixture.hashedPasswords);
        assertEquals(1, fixture.transaction.executions.get());
        assertEquals(List.of("SUCCESS:false"), fixture.audit.events);
        assertEquals(1L, result.getClass().getMethod("adminUserId").invoke(result));
        assertEquals(OptionalLong.empty(), result.getClass().getMethod("testUserId").invoke(result));
    }

    @Test
    @Tag("AC-001")
    @Tag("AC-002")
    @Tag("AC-003")
    @DisplayName("T-002-T2: creates ADMIN and requested USER atomically with fixed roles")
    void test_AC001_AC002_AC003_T002T2_bootstrapsBothAccounts() throws Exception {
        Fixture fixture = new Fixture();

        fixture.bootstrap(
                details("Admin", "admin@example.com", "admin-secret"),
                Optional.of(details("Test User", "user@example.com", "user-secret")));

        assertEquals(List.of(UserRole.ADMIN, UserRole.USER),
                fixture.repository.committed.stream().map(UserSnapshot::role).toList());
        assertEquals(List.of("encoded:admin-secret", "encoded:user-secret"),
                fixture.repository.committed.stream().map(UserSnapshot::passwordHash).toList());
        assertEquals(List.of("admin-secret", "user-secret"), fixture.hashedPasswords);
        assertEquals(List.of("SUCCESS:true"), fixture.audit.events);
        assertTrue(fixture.audit.successWasInAccountTransaction);
    }

    @Test
    @Tag("AC-005")
    @DisplayName("T-002-T3: refuses bootstrap when an ADMIN already exists")
    void test_AC005_T002T3_doesNotCreateAccountsWhenAdminExists() throws Exception {
        Fixture fixture = new Fixture();
        fixture.repository.adminAlreadyExists = true;

        Throwable failure = assertThrows(Throwable.class,
                () -> fixture.bootstrap(details("Admin", "admin@example.com", "secret"), Optional.empty()));

        assertEquals("ADMIN_ALREADY_EXISTS", reasonCode(failure));
        assertTrue(fixture.repository.committed.isEmpty());
        assertTrue(fixture.hashedPasswords.isEmpty());
        assertEquals(List.of("FAILURE:ADMIN_ALREADY_EXISTS:false"), fixture.audit.events);
        assertFalse(fixture.audit.failureWasInAccountTransaction);
    }

    @Test
    @Tag("AC-010")
    @DisplayName("T-002-T4: invalid details fail before hashing or persistence")
    void test_AC010_T002T4_invalidDetailsCreateNoPartialAccounts() throws Exception {
        Fixture fixture = new Fixture();

        Throwable failure = assertThrows(Throwable.class,
                () -> fixture.bootstrap(details(" ", "invalid-email", "secret"), Optional.empty()));

        assertEquals("INVALID_INPUT", reasonCode(failure));
        assertTrue(fixture.repository.committed.isEmpty());
        assertTrue(fixture.hashedPasswords.isEmpty());
        assertEquals(List.of("FAILURE:INVALID_INPUT:false"), fixture.audit.events);
    }

    @Test
    @Tag("AC-010")
    @DisplayName("T-002-T4 edge: duplicate account emails fail without partial writes")
    void test_AC010_T002T4_duplicateEmailsCreateNoPartialAccounts() throws Exception {
        Fixture fixture = new Fixture();

        Throwable failure = assertThrows(Throwable.class, () -> fixture.bootstrap(
                details("Admin", "same@example.com", "admin-secret"),
                Optional.of(details("Test User", "same@example.com", "user-secret"))));

        assertEquals("DUPLICATE_EMAIL", reasonCode(failure));
        assertTrue(fixture.repository.committed.isEmpty());
        assertTrue(fixture.hashedPasswords.isEmpty());
        assertEquals(List.of("FAILURE:DUPLICATE_EMAIL:true"), fixture.audit.events);
    }

    @Test
    @Tag("AC-010")
    @DisplayName("T-002-T4 edge: an email already in storage is rejected")
    void test_AC010_T002T4_existingEmailCreatesNoAccounts() throws Exception {
        Fixture fixture = new Fixture();
        fixture.repository.committed.add(new UserSnapshot("admin@example.com", "existing-hash", UserRole.USER));

        Throwable failure = assertThrows(Throwable.class,
                () -> fixture.bootstrap(details("Admin", "admin@example.com", "secret"), Optional.empty()));

        assertEquals("DUPLICATE_EMAIL", reasonCode(failure));
        assertEquals(1, fixture.repository.committed.size());
        assertTrue(fixture.hashedPasswords.isEmpty());
        assertEquals(List.of("FAILURE:DUPLICATE_EMAIL:false"), fixture.audit.events);
    }

    @Test
    @Tag("AC-001")
    @Tag("AC-002")
    @DisplayName("T-002-T5: persistence failure rolls back both requested accounts")
    void test_AC001_AC002_T002T5_persistenceFailureRollsBackAllAccounts() throws Exception {
        Fixture fixture = new Fixture();
        fixture.repository.failOnPersistNumber = 2;

        Throwable failure = assertThrows(Throwable.class, () -> fixture.bootstrap(
                details("Admin", "admin@example.com", "admin-secret"),
                Optional.of(details("Test User", "user@example.com", "user-secret"))));

        assertEquals("BOOTSTRAP_FAILURE", reasonCode(failure));
        assertTrue(fixture.repository.committed.isEmpty());
        assertEquals(List.of("FAILURE:BOOTSTRAP_FAILURE:true"), fixture.audit.events);
        assertFalse(fixture.audit.failureWasInAccountTransaction);
    }

    @Test
    @Tag("AC-001")
    @Tag("AC-002")
    @DisplayName("T-002-T5 edge: hashing failure rolls back all requested accounts")
    void test_AC001_AC002_T002T5_hashingFailureRollsBackAllAccounts() throws Exception {
        Fixture fixture = new Fixture();
        fixture.hashFailureForPassword = "user-secret";

        Throwable failure = assertThrows(Throwable.class, () -> fixture.bootstrap(
                details("Admin", "admin@example.com", "admin-secret"),
                Optional.of(details("Test User", "user@example.com", "user-secret"))));

        assertEquals("BOOTSTRAP_FAILURE", reasonCode(failure));
        assertTrue(fixture.repository.committed.isEmpty());
        assertEquals(List.of("FAILURE:BOOTSTRAP_FAILURE:true"), fixture.audit.events);
        assertFalse(fixture.audit.failureWasInAccountTransaction);
    }

    @Test
    @Tag("AC-003")
    @DisplayName("T-002-T6: generated hash verifies with the approved Argon2id hasher")
    void test_AC003_T002T6_generatedPasswordHashVerifies() {
        Argon2PasswordHasher hasher = new Argon2PasswordHasher(65_536, 3, 1, 32);
        String encodedHash = hasher.hash("account-bootstrap-secret");

        assertTrue(hasher.matches("account-bootstrap-secret", encodedHash));
        assertFalse(hasher.matches("different-secret", encodedHash));
    }

    private static Object details(String name, String email, String password) throws Exception {
        Class<?> type = requiredType("com.victor.ecommerce.application.account.AccountBootstrapService$AccountDetails");
        return type.getConstructor(String.class, String.class, String.class).newInstance(name, email, password);
    }

    private static String reasonCode(Throwable failure) throws Exception {
        Throwable root = failure instanceof InvocationTargetException invocation
                ? invocation.getTargetException()
                : failure;
        Method reasonCode = root.getClass().getMethod("reasonCode");
        return reasonCode.invoke(root).toString();
    }

    private static Class<?> requiredType(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException missingType) {
            assertNotNull(null, "SLDD T-002 contract requires type " + name);
            throw new AssertionError("unreachable");
        }
    }

    private static Object proxy(Class<?> type, InvocationHandler handler) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static final class Fixture {
        private final RepositoryProbe repository = new RepositoryProbe();
        private final TransactionProbe transaction = new TransactionProbe(repository);
        private final AuditProbe audit = new AuditProbe(transaction);
        private final List<String> hashedPasswords = new ArrayList<>();
        private final PasswordHasher hasher = mock(PasswordHasher.class);
        private final Object service;
        private String hashFailureForPassword;

        private Fixture() throws Exception {
            when(hasher.hash(anyString())).thenAnswer(invocation -> {
                String rawPassword = invocation.getArgument(0);
                hashedPasswords.add(rawPassword);
                if (rawPassword.equals(hashFailureForPassword)) {
                    throw new IllegalStateException("simulated hashing failure");
                }
                return "encoded:" + rawPassword;
            });

            Class<?> transactionPort = requiredType("com.victor.ecommerce.application.account.AccountBootstrapTransaction");
            Class<?> repositoryPort = requiredType("com.victor.ecommerce.application.account.UserAccountRepository");
            Class<?> auditPort = requiredType("com.victor.ecommerce.application.account.AccountProvisioningAudit");
            Class<?> serviceType = requiredType("com.victor.ecommerce.application.account.AccountBootstrapService");
            Constructor<?> constructor = serviceType.getConstructor(
                    transactionPort, repositoryPort, auditPort, PasswordHasher.class);
            service = constructor.newInstance(transaction.proxy(transactionPort),
                    repository.proxy(repositoryPort), audit.proxy(auditPort), hasher);
        }

        private Object bootstrap(Object admin, Optional<?> testUser) throws Exception {
            Class<?> requestType = requiredType("com.victor.ecommerce.application.account.AccountBootstrapService$BootstrapRequest");
            Object request = requestType.getConstructor(
                    requiredType("com.victor.ecommerce.application.account.AccountBootstrapService$AccountDetails"), Optional.class)
                    .newInstance(admin, testUser);
            try {
                return service.getClass().getMethod("bootstrap", requestType).invoke(service, request);
            } catch (InvocationTargetException failure) {
                throw failure;
            }
        }
    }

    private static final class RepositoryProbe {
        private final List<UserSnapshot> committed = new ArrayList<>();
        private final List<UserSnapshot> staged = new ArrayList<>();
        private boolean adminAlreadyExists;
        private int failOnPersistNumber = -1;
        private int persistCount;
        private long nextId = 1;

        private Object proxy(Class<?> port) {
            return AccountBootstrapServiceTest.proxy(port, (proxy, method, args) -> switch (method.getName()) {
                case "administratorExists" -> adminAlreadyExists;
                case "emailExists" -> committed.stream().anyMatch(user -> user.email.equals(args[0]))
                        || staged.stream().anyMatch(user -> user.email.equals(args[0]));
                case "persist" -> persist(args[0]);
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }

        private long persist(Object user) throws Exception {
            persistCount++;
            if (persistCount == failOnPersistNumber) {
                throw new IllegalStateException("simulated persistence failure");
            }
            staged.add(new UserSnapshot(
                    (String) user.getClass().getMethod("getEmail").invoke(user),
                    (String) user.getClass().getMethod("getPasswordHash").invoke(user),
                    (UserRole) user.getClass().getMethod("getRole").invoke(user)));
            return nextId++;
        }
    }

    private static final class TransactionProbe {
        private final RepositoryProbe repository;
        private final AtomicInteger executions = new AtomicInteger();
        private boolean active;

        private TransactionProbe(RepositoryProbe repository) {
            this.repository = repository;
        }

        private Object proxy(Class<?> port) {
            return AccountBootstrapServiceTest.proxy(port, (proxy, method, args) -> {
                if (!method.getName().equals("execute")) {
                    throw new UnsupportedOperationException(method.getName());
                }
                executions.incrementAndGet();
                active = true;
                repository.staged.clear();
                try {
                    Object result = ((Supplier<?>) args[0]).get();
                    repository.committed.addAll(repository.staged);
                    return result;
                } finally {
                    active = false;
                    repository.staged.clear();
                }
            });
        }
    }

    private static final class AuditProbe {
        private final TransactionProbe transaction;
        private final List<String> events = new ArrayList<>();
        private boolean successWasInAccountTransaction;
        private boolean failureWasInAccountTransaction;

        private AuditProbe(TransactionProbe transaction) {
            this.transaction = transaction;
        }

        private Object proxy(Class<?> port) {
            return AccountBootstrapServiceTest.proxy(port, (proxy, method, args) -> {
                if (method.getName().equals("recordSuccess")) {
                    successWasInAccountTransaction = transaction.active;
                    events.add("SUCCESS:" + args[0]);
                    return null;
                }
                if (method.getName().equals("recordFailure")) {
                    failureWasInAccountTransaction = transaction.active;
                    events.add("FAILURE:" + args[0] + ":" + args[1]);
                    return null;
                }
                throw new UnsupportedOperationException(method.getName());
            });
        }
    }

    private record UserSnapshot(String email, String passwordHash, UserRole role) {
    }
}
