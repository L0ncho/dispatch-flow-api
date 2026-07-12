package com.dispatchflow.guides.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "efs")
public class EfsStorageProperties {

    private String basePath = "./tmp/efs";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
