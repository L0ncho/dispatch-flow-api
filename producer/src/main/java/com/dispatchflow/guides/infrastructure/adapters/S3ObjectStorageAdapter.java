package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.infrastructure.config.AwsS3Properties;
import com.dispatchflow.shared.domain.DomainError;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.UncheckedIOException;

public class S3ObjectStorageAdapter implements ObjectStoragePort {

    private final S3Client s3Client;
    private final String bucketName;

    public S3ObjectStorageAdapter(S3Client s3Client, AwsS3Properties properties) {
        this.s3Client = s3Client;
        this.bucketName = properties.getBucketName();
    }

    @Override
    public void store(String key, byte[] content) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                    RequestBody.fromBytes(content));
        } catch (S3Exception exception) {
            throw DomainError.other("Could not store object in S3");
        }
    }

    @Override
    public byte[] read(String key) {
        try {
            return s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucketName).key(key).build()).asByteArray();
        } catch (NoSuchKeyException exception) {
            throw DomainError.notFound("Object " + key + " not found in S3");
        } catch (S3Exception exception) {
            throw DomainError.other("Could not read object from S3");
        } catch (UncheckedIOException exception) {
            throw DomainError.other("Could not read object from S3");
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
        } catch (S3Exception exception) {
            throw DomainError.other("Could not delete object from S3");
        }
    }
}
