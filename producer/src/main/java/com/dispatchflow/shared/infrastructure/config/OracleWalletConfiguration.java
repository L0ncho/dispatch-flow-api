package com.dispatchflow.shared.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "oracle.jdbc.OracleDriver")
@EnableConfigurationProperties(OracleWalletProperties.class)
public class OracleWalletConfiguration {

    private final OracleWalletProperties walletProperties;

    public OracleWalletConfiguration(OracleWalletProperties walletProperties) {
        this.walletProperties = walletProperties;
    }

    @PostConstruct
    void configureWalletLocation() {
        Path walletDirectory = resolveWalletDirectory();
        OracleWalletValidator.validate(walletDirectory);

        String tnsAdmin = System.getenv("TNS_ADMIN");
        if (tnsAdmin == null || tnsAdmin.isBlank()) {
            System.setProperty("oracle.net.tns_admin", walletDirectory.toString());
        }
    }

    private Path resolveWalletDirectory() {
        String tnsAdmin = System.getenv("TNS_ADMIN");
        if (tnsAdmin != null && !tnsAdmin.isBlank()) {
            return Paths.get(tnsAdmin).toAbsolutePath().normalize();
        }
        return walletProperties.resolvedLocation();
    }
}
