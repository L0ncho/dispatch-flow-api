package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.shared.domain.DomainError;

import java.util.HashMap;
import java.util.Map;

public class InMemoryObjectStorageAdapter implements ObjectStoragePort {

    private final Map<String, byte[]> objects = new HashMap<>();

    @Override
    public void store(String key, byte[] content) {
        objects.put(key, content);
    }

    @Override
    public byte[] read(String key) {
        byte[] content = objects.get(key);
        if (content == null) {
            throw DomainError.notFound("Object " + key + " not found in storage");
        }
        return content;
    }

    @Override
    public void delete(String key) {
        objects.remove(key);
    }
}
