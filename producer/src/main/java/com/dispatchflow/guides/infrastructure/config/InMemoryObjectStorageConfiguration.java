package com.dispatchflow.guides.infrastructure.config;

import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.infrastructure.adapters.InMemoryObjectStorageAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "dispatch.storage.s3.enabled", havingValue = "false")
public class InMemoryObjectStorageConfiguration {

    @Bean
    public ObjectStoragePort objectStoragePort() {
        return new InMemoryObjectStorageAdapter();
    }
}
