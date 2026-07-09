package com.dispatchflow.shared.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleWalletValidatorTest {

    @Test
    void acceptsWalletWithRequiredFiles(@TempDir Path walletDirectory) throws Exception {
        Files.writeString(walletDirectory.resolve("tnsnames.ora"), "dispatchflowdb_high = (description=())");
        Files.writeString(walletDirectory.resolve("cwallet.sso"), "wallet-content");

        assertDoesNotThrow(() -> OracleWalletValidator.validate(walletDirectory));
    }

    @Test
    void rejectsEmptyWalletDirectory(@TempDir Path walletDirectory) {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> OracleWalletValidator.validate(walletDirectory));

        assertTrue(error.getMessage().contains("tnsnames.ora"));
    }
}
