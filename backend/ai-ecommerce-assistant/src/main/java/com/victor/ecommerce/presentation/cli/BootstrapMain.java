package com.victor.ecommerce.presentation.cli;

import com.victor.ecommerce.application.account.AccountBootstrapService;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

@QuarkusMain(name = "account-bootstrap")
public final class BootstrapMain implements QuarkusApplication {

    @Inject
    AccountBootstrapService bootstrapService;

    public static void main(String... arguments) {
        Quarkus.run(BootstrapMain.class, arguments);
    }

    @Override
    public int run(String... arguments) {
        return new AccountBootstrapCommand(
                bootstrapService,
                new ConsolePasswordPrompt(System.console()),
                System.out).run(arguments);
    }
}
