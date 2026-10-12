package com.victor.ecommerce.presentation.cli;

import com.victor.ecommerce.application.account.AccountBootstrapException;
import com.victor.ecommerce.application.account.AccountBootstrapService;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Optional;

public final class AccountBootstrapCommand {

    public static final int SUCCESS = 0;
    public static final int BOOTSTRAP_FAILED = 1;
    public static final int INVALID_INPUT = 2;

    private final AccountBootstrapService bootstrapService;
    private final ConsolePasswordPrompt passwordPrompt;
    private final PrintStream output;

    public AccountBootstrapCommand(
            AccountBootstrapService bootstrapService,
            ConsolePasswordPrompt passwordPrompt,
            PrintStream output) {
        this.bootstrapService = bootstrapService;
        this.passwordPrompt = passwordPrompt;
        this.output = output;
    }

    public int run(String[] arguments) {
        ParsedArguments parsed;
        try {
            parsed = parseArguments(arguments);
        } catch (IllegalArgumentException invalidArguments) {
            output.println("Bootstrap failed: INVALID_INPUT");
            return INVALID_INPUT;
        }

        if (passwordPrompt == null || !passwordPrompt.isAvailable()) {
            output.println("Bootstrap failed: INVALID_INPUT");
            return INVALID_INPUT;
        }

        try {
            AccountBootstrapService.AccountDetails admin = readAccount("administrator");
            Optional<AccountBootstrapService.AccountDetails> testUser = parsed.testUserRequested()
                    ? Optional.of(readAccount("test user"))
                    : Optional.empty();
            AccountBootstrapService.BootstrapResult result = bootstrapService.bootstrap(
                    new AccountBootstrapService.BootstrapRequest(admin, testUser));
            output.println("Bootstrap completed: adminUserId=" + result.adminUserId());
            result.testUserId().ifPresent(id -> output.println("testUserId=" + id));
            return SUCCESS;
        } catch (AccountBootstrapException failure) {
            output.println("Bootstrap failed: " + failure.reasonCode().name());
            return BOOTSTRAP_FAILED;
        } catch (RuntimeException failure) {
            output.println("Bootstrap failed: BOOTSTRAP_FAILURE");
            return BOOTSTRAP_FAILED;
        }
    }

    public static ParsedArguments parseArguments(String[] arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("arguments are required");
        }
        boolean testUserRequested = false;
        for (String argument : arguments) {
            if ("--test-user".equals(argument) && !testUserRequested) {
                testUserRequested = true;
            } else {
                throw new IllegalArgumentException("unsupported argument");
            }
        }
        return new ParsedArguments(testUserRequested);
    }

    private AccountBootstrapService.AccountDetails readAccount(String label) {
        String name = passwordPrompt.readLine(label + " name: ");
        String email = passwordPrompt.readLine(label + " email: ");
        char[] password = passwordPrompt.readConfirmedPassword(label + " password: ");
        try {
            return new AccountBootstrapService.AccountDetails(name, email, new String(password));
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public record ParsedArguments(boolean testUserRequested) {
    }
}
