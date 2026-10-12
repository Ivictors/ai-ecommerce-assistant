package com.victor.ecommerce.presentation.cli;

import io.quarkus.runtime.annotations.QuarkusMain;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class BootstrapMainTest {

    @Test
    void test_AC004_T004T4_bootstrapEntryPointIsExplicitCommandMode() throws Exception {
        Class<?> mainType = requiredType("com.victor.ecommerce.presentation.cli.BootstrapMain");
        assertTrue(io.quarkus.runtime.QuarkusApplication.class.isAssignableFrom(mainType));
        Annotation annotation = mainType.getAnnotation(QuarkusMain.class);
        assertNotNull(annotation, "bootstrap must be an explicit Quarkus command");
        assertEquals("account-bootstrap", ((QuarkusMain) annotation).name());
    }

    @Test
    void test_AC008_AC011_T004T5_bootstrapConfigurationUsesDedicatedProfile() {
        var resource = BootstrapMainTest.class.getClassLoader()
                .getResource("application-bootstrap.properties");
        assertNotNull(resource, "T-004 requires dedicated bootstrap configuration");
    }

    private static Class<?> requiredType(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException missingType) {
            fail("T-004 requires production type " + name);
            return null;
        }
    }
}
