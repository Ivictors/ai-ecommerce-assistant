package com.victor.ecommerce.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    @Tag("AC-003")
    @DisplayName("T-002-T1: requires an explicit role and credential hash")
    void test_AC003_T002T1_constructsCredentialedUserWithExplicitRole() throws Exception {
        Constructor<?> constructor = requiredAccountConstructor();

        Object user = constructor.newInstance("Admin", "admin@example.com", "encoded-hash", UserRole.ADMIN);

        assertEquals("encoded-hash", user.getClass().getMethod("getPasswordHash").invoke(user));
        assertEquals(UserRole.ADMIN, user.getClass().getMethod("getRole").invoke(user));
    }

    @Test
    @Tag("AC-003")
    @DisplayName("T-002-T1 edge: rejects blank credential hash")
    void test_AC003_T002T1_rejectsBlankCredentialHash() throws Exception {
        Constructor<?> constructor = requiredAccountConstructor();

        assertThrows(InvocationTargetException.class,
                () -> constructor.newInstance("Admin", "admin@example.com", "  ", UserRole.ADMIN));
    }

    @Test
    @Tag("AC-003")
    @DisplayName("T-002-T1 edge: rejects missing role")
    void test_AC003_T002T1_rejectsMissingRole() throws Exception {
        Constructor<?> constructor = requiredAccountConstructor();

        assertThrows(InvocationTargetException.class,
                () -> constructor.newInstance("Admin", "admin@example.com", "encoded-hash", null));
    }

    @Test
    @Tag("AC-003")
    @DisplayName("T-002-T1 edge: rejects blank account name")
    void test_AC003_T002T1_rejectsBlankName() throws Exception {
        Constructor<?> constructor = requiredAccountConstructor();

        assertThrows(InvocationTargetException.class,
                () -> constructor.newInstance(" ", "admin@example.com", "encoded-hash", UserRole.ADMIN));
    }

    @Test
    @Tag("AC-003")
    @DisplayName("T-002-T1 edge: rejects blank email")
    void test_AC003_T002T1_rejectsBlankEmail() throws Exception {
        Constructor<?> constructor = requiredAccountConstructor();

        assertThrows(InvocationTargetException.class,
                () -> constructor.newInstance("Admin", " ", "encoded-hash", UserRole.ADMIN));
    }

    private Constructor<?> requiredAccountConstructor() {
        Constructor<?> constructor = Arrays.stream(User.class.getConstructors())
                .filter(candidate -> Arrays.equals(candidate.getParameterTypes(),
                        new Class<?>[]{String.class, String.class, String.class, UserRole.class}))
                .findFirst()
                .orElse(null);
        assertNotNull(constructor,
                "SLDD T-002 contract requires User(String, String, String, UserRole)");
        return constructor;
    }
}
