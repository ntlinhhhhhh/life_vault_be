package com.moon.vault.service;

public interface EncryptionService {

    EncryptedValue encryptString(String plaintext);

    String decryptString(String ciphertext, String iv);

    EncryptedBytes encryptBytes(byte[] plaintext);

    byte[] decryptBytes(byte[] ciphertext, String iv);

    record EncryptedValue(String ciphertext, String iv) {
    }

    record EncryptedBytes(byte[] ciphertext, String iv) {
    }
}
