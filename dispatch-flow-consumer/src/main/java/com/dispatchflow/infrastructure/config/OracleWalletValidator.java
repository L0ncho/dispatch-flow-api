package com.dispatchflow.shared.infrastructure.config;

import java.nio.file.Files;
import java.nio.file.Path;

public final class OracleWalletValidator {

    private OracleWalletValidator() {
    }

    public static void validate(Path walletDirectory) {
        if (!Files.isDirectory(walletDirectory)) {
            throw new IllegalStateException("Oracle wallet directory not found: " + walletDirectory);
        }

        Path tnsNames = walletDirectory.resolve("tnsnames.ora");
        if (!Files.exists(tnsNames)) {
            throw new IllegalStateException(
                    "Oracle wallet is missing tnsnames.ora in " + walletDirectory
                            + ". Run ./scripts/setup-oracle-wallet.sh");
        }

        Path autoLoginWallet = walletDirectory.resolve("cwallet.sso");
        Path passwordWallet = walletDirectory.resolve("ewallet.p12");
        if (!Files.exists(autoLoginWallet) && !Files.exists(passwordWallet)) {
            throw new IllegalStateException(
                    "Oracle wallet is missing cwallet.sso or ewallet.p12 in " + walletDirectory
                            + ". Run ./scripts/setup-oracle-wallet.sh");
        }
    }
}
