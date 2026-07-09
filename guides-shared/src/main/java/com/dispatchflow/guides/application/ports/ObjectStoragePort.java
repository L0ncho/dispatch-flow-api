package com.dispatchflow.guides.application.ports;

public interface ObjectStoragePort {

    void store(String key, byte[] content);

    byte[] read(String key);

    void delete(String key);
}
