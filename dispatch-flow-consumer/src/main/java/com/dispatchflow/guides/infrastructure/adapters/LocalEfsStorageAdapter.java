package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.application.ports.EfsStoragePort;
import com.dispatchflow.guides.infrastructure.config.EfsStorageProperties;
import com.dispatchflow.shared.domain.DomainError;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class LocalEfsStorageAdapter implements EfsStoragePort {

    private final Path basePath;

    public LocalEfsStorageAdapter(EfsStorageProperties properties) {
        this.basePath = Paths.get(properties.getBasePath()).toAbsolutePath().normalize();
    }

    @Override
    public String write(String relativePath, byte[] content) {
        Path targetPath = resolveWithinBase(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content);
            return targetPath.toString();
        } catch (IOException exception) {
            throw DomainError.other("Could not write file to EFS storage");
        }
    }

    @Override
    public byte[] read(String absolutePath) {
        Path targetPath = Paths.get(absolutePath).toAbsolutePath().normalize();
        validateWithinBase(targetPath);
        if (!Files.exists(targetPath)) {
            throw DomainError.notFound("PDF file not found at " + absolutePath);
        }
        try {
            return Files.readAllBytes(targetPath);
        } catch (IOException exception) {
            throw DomainError.other("Could not read file from EFS storage");
        }
    }

    private Path resolveWithinBase(String relativePath) {
        Path targetPath = basePath.resolve(relativePath).normalize();
        validateWithinBase(targetPath);
        return targetPath;
    }

    private void validateWithinBase(Path targetPath) {
        if (!targetPath.startsWith(basePath)) {
            throw DomainError.validation("Invalid storage path");
        }
    }
}
