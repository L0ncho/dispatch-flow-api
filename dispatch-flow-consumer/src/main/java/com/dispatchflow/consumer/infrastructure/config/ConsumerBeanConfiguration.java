package com.dispatchflow.consumer.infrastructure.config;

import com.dispatchflow.consumer.application.ProcessGuideMessageUseCase;
import com.dispatchflow.consumer.infrastructure.persistence.AsyncDispatchGuideRepository;
import com.dispatchflow.consumer.infrastructure.persistence.AsyncGuideSequenceRepository;
import com.dispatchflow.guides.application.GuidePdfEfsStorage;
import com.dispatchflow.guides.application.GuidePdfS3Storage;
import com.dispatchflow.guides.application.ports.EfsStoragePort;
import com.dispatchflow.guides.application.ports.GuidePdfGeneratorPort;
import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.domain.services.GuideNumberGenerator;
import com.dispatchflow.guides.domain.services.GuidePdfPathBuilder;
import com.dispatchflow.guides.infrastructure.config.EfsStorageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(EfsStorageProperties.class)
public class ConsumerBeanConfiguration {

    @Bean
    public GuideNumberGenerator guideNumberGenerator() {
        return new GuideNumberGenerator();
    }

    @Bean
    public GuidePdfPathBuilder guidePdfPathBuilder() {
        return new GuidePdfPathBuilder();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public GuidePdfEfsStorage guidePdfEfsStorage(
            GuidePdfPathBuilder guidePdfPathBuilder,
            GuidePdfGeneratorPort guidePdfGeneratorPort,
            EfsStoragePort efsStoragePort) {
        return new GuidePdfEfsStorage(guidePdfPathBuilder, guidePdfGeneratorPort, efsStoragePort);
    }

    @Bean
    public GuidePdfS3Storage guidePdfS3Storage(
            GuidePdfPathBuilder guidePdfPathBuilder,
            ObjectStoragePort objectStoragePort) {
        return new GuidePdfS3Storage(guidePdfPathBuilder, objectStoragePort);
    }

    @Bean
    public ProcessGuideMessageUseCase processGuideMessageUseCase(
            AsyncGuideSequenceRepository sequenceRepository,
            AsyncDispatchGuideRepository asyncDispatchGuideRepository,
            GuideNumberGenerator guideNumberGenerator,
            GuidePdfEfsStorage guidePdfEfsStorage,
            GuidePdfS3Storage guidePdfS3Storage,
            @Value("${aws.s3.bucket-name}") String s3BucketName,
            Clock clock) {
        return new ProcessGuideMessageUseCase(
                sequenceRepository,
                asyncDispatchGuideRepository,
                guideNumberGenerator,
                guidePdfEfsStorage,
                guidePdfS3Storage,
                s3BucketName,
                clock);
    }
}
