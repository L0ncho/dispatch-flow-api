package com.dispatchflow.guides.infrastructure.config;

import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.infrastructure.adapters.S3ObjectStorageAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "dispatch.storage.s3.enabled", havingValue = "true")
@EnableConfigurationProperties(AwsS3Properties.class)
public class AwsS3Configuration {

    @Bean
    public S3Client s3Client(AwsS3Properties properties) {
        var builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build());

        if (properties.hasCustomEndpoint()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        return builder.build();
    }

    @Bean
    public ObjectStoragePort objectStoragePort(S3Client s3Client, AwsS3Properties properties) {
        return new S3ObjectStorageAdapter(s3Client, properties);
    }

    private AwsCredentialsProvider credentialsProvider(AwsS3Properties properties) {
        if (!properties.hasStaticCredentials()) {
            return DefaultCredentialsProvider.create();
        }
        if (properties.hasSessionToken()) {
            return StaticCredentialsProvider.create(AwsSessionCredentials.create(
                    properties.getAccessKeyId(),
                    properties.getSecretAccessKey(),
                    properties.getSessionToken()));
        }
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(
                properties.getAccessKeyId(),
                properties.getSecretAccessKey()));
    }
}
