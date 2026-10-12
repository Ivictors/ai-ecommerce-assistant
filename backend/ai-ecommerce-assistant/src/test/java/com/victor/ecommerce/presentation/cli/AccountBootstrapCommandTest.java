package com.victor.ecommerce.presentation.cli;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.victor.ecommerce.application.account.AccountBootstrapException;
import com.victor.ecommerce.application.account.AccountBootstrapService;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AccountBootstrapCommandTest {

    @Test
    void test_AC001_AC004_T004T1_commandAcceptsOnlyOptionalTestUserFlag() throws Exception {
        Class<?> commandType = requiredType("com.victor.ecommerce.presentation.cli.AccountBootstrapCommand");
        Method parse = Arrays.stream(commandType.getDeclaredMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getName().equals("parseArguments"))
                .findFirst()
                .orElseGet(() -> {
                    fail("T-004 requires an explicit command argument parser");
                    return null;
                });

        Object noFlag = invoke(parse, new Object[]{new String[0]});
        Object testUser = invoke(parse, new Object[]{new String[]{"--test-user"}});
        assertFalse(booleanProperty(noFlag, "testUserRequested"));
        assertTrue(booleanProperty(testUser, "testUserRequested"));

        assertThrowsInvocation(() -> invoke(parse, new Object[]{new String[]{"--unexpected"}}));
    }

    @Test
    void test_AC003_AC008_T004T2_promptContractReturnsFreshConfirmedBuffers() throws Exception {
        Class<?> promptType = requiredType("com.victor.ecommerce.presentation.cli.ConsolePasswordPrompt");
        Method confirmation = Arrays.stream(promptType.getDeclaredMethods())
                .filter(method -> method.getName().equals("readConfirmedPassword"))
                .findFirst()
                .orElseGet(() -> {
                    fail("T-004 requires a hidden confirmation prompt");
                    return null;
                });
        assertEquals(char[].class, confirmation.getReturnType());
        assertNotNull(promptType.getConstructor(java.io.Console.class));
    }

    @Test
    void test_AC003_AC010_T004T3_commandHasStableExitCodesAndFailsClosedWithoutConsole() throws Exception {
        Class<?> commandType = requiredType("com.victor.ecommerce.presentation.cli.AccountBootstrapCommand");
        assertEquals(int.class, commandType.getMethod("run", String[].class).getReturnType());
        assertEquals(0, commandType.getField("SUCCESS").getInt(null));
        assertTrue(commandType.getField("INVALID_INPUT").getInt(null) > 0);
        assertTrue(commandType.getField("BOOTSTRAP_FAILED").getInt(null) > 0);
        assertNotNull(requiredType("com.victor.ecommerce.presentation.cli.BootstrapMain"));
    }

    @Test
    void test_AC003_T004T3_noConsoleFailsBeforeTheBootstrapServiceIsCalled() {
        AccountBootstrapService service = mock(AccountBootstrapService.class);
        AccountBootstrapCommand command = new AccountBootstrapCommand(
                service, new ConsolePasswordPrompt(null), System.out);

        assertEquals(AccountBootstrapCommand.INVALID_INPUT, command.run(new String[0]));
        verifyNoInteractions(service);
    }

    @Test
    void test_AC008_T004T2_confirmationClearsConsoleBuffersAfterReturningCopy() {
        java.io.Console console = mock(java.io.Console.class);
        char[] first = "secret".toCharArray();
        char[] confirmation = "secret".toCharArray();
        when(console.readPassword("password: ")).thenReturn(first);
        when(console.readPassword("Confirm password: ")).thenReturn(confirmation);

        char[] returned = new ConsolePasswordPrompt(console).readConfirmedPassword("password: ");

        assertEquals("secret", new String(returned));
        assertTrue(allZero(first));
        assertTrue(allZero(confirmation));
        Arrays.fill(returned, '\0');
    }

    @Test
    void test_AC010_T004T5_failureOutputContainsOnlySafeReasonCode() {
        AccountBootstrapService service = mock(AccountBootstrapService.class);
        ConsolePasswordPrompt prompt = mock(ConsolePasswordPrompt.class);
        when(prompt.isAvailable()).thenReturn(true);
        when(prompt.readLine("administrator name: ")).thenReturn("Admin");
        when(prompt.readLine("administrator email: ")).thenReturn("admin@example.test");
        when(prompt.readConfirmedPassword("administrator password: ")).thenReturn("secret".toCharArray());
        when(service.bootstrap(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new AccountBootstrapException(
                        com.victor.ecommerce.application.account.BootstrapFailureCode.ADMIN_ALREADY_EXISTS));
        var output = new java.io.ByteArrayOutputStream();

        int exitCode = new AccountBootstrapCommand(service, prompt, new java.io.PrintStream(output))
                .run(new String[0]);

        assertEquals(AccountBootstrapCommand.BOOTSTRAP_FAILED, exitCode);
        assertEquals("Bootstrap failed: ADMIN_ALREADY_EXISTS" + System.lineSeparator(), output.toString());
    }

    private static Class<?> requiredType(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException missingType) {
            fail("T-004 requires production type " + name);
            return null;
        }
    }

    private static Object invoke(Method method, Object[] args) throws Exception {
        try {
            return method.invoke(null, args);
        } catch (InvocationTargetException failure) {
            throw failure;
        }
    }

    private static boolean booleanProperty(Object target, String property) throws Exception {
        return (boolean) target.getClass().getMethod(property).invoke(target);
    }

    private static boolean allZero(char[] values) {
        for (char value : values) {
            if (value != '\0') {
                return false;
            }
        }
        return true;
    }

    private static void assertThrowsInvocation(ThrowingOperation operation) throws Exception {
        try {
            operation.run();
            fail("T-004 must reject unsupported arguments");
        } catch (InvocationTargetException expected) {
            assertNotNull(expected.getTargetException());
        }
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run() throws Exception;
    }
}
