package com.dispatchflow.guides.application.ports;

public interface EfsStoragePort {

    String write(String relativePath, byte[] content);

    byte[] read(String absolutePath);
}
