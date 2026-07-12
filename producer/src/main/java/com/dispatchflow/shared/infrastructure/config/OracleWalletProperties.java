package com.dispatchflow.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "oracle.wallet")
public class OracleWalletProperties {

    private String location = "./Wallet_DISPATCHFLOWDB";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Path resolvedLocation() {
        return Paths.get(location).toAbsolutePath().normalize();
    }
}
