package com.moon.vault.service;

public interface ObjectStorageService {

    void put(String objectKey, byte[] content, String contentType);

    byte[] get(String objectKey);

    void delete(String objectKey);
}
