package com.victor.ecommerce.presentation.cli;

import java.io.Console;
import java.util.Arrays;

public final class ConsolePasswordPrompt {

    private final Console console;

    public ConsolePasswordPrompt(Console console) {
        this.console = console;
    }

    public boolean isAvailable() {
        return console != null;
    }

    public String readLine(String prompt) {
        if (console == null) {
            throw new IllegalStateException("interactive console is required");
        }
        String value = console.readLine(prompt);
        if (value == null) {
            throw new IllegalStateException("interactive console closed");
        }
        return value;
    }

    public char[] readConfirmedPassword(String prompt) {
        if (console == null) {
            throw new IllegalStateException("interactive console is required");
        }
        char[] first = console.readPassword(prompt);
        char[] confirmation = null;
        try {
            if (first == null) {
                throw new IllegalStateException("interactive console closed");
            }
            confirmation = console.readPassword("Confirm " + prompt);
            if (confirmation == null || !Arrays.equals(first, confirmation)) {
                throw new IllegalArgumentException("password confirmation mismatch");
            }
            return Arrays.copyOf(first, first.length);
        } finally {
            Arrays.fill(first, '\0');
            if (confirmation != null) {
                Arrays.fill(confirmation, '\0');
            }
        }
    }
}
