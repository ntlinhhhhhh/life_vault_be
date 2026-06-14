package com.moon.vault.service.impl;

import com.moon.vault.exception.VaultException;
import com.moon.vault.service.EncryptionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionServiceImpl implements EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    @Value("${vault.encryption.secret}")
    private String secret;

    private final SecureRandom secureRandom = new SecureRandom();
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("vault.encryption.secret must be at least 32 bytes");
        }
        byte[] aesKey = new byte[32];
        System.arraycopy(keyBytes, 0, aesKey, 0, 32);
        secretKey = new SecretKeySpec(aesKey, "AES");
    }

    @Override
    public EncryptedValue encryptString(String plaintext) {
        if (plaintext == null) {
            return new EncryptedValue(null, null);
        }
        EncryptedBytes encrypted = encryptBytes(plaintext.getBytes(StandardCharsets.UTF_8));
        return new EncryptedValue(Base64.getEncoder().encodeToString(encrypted.ciphertext()), encrypted.iv());
    }

    @Override
    public String decryptString(String ciphertext, String iv) {
        if (ciphertext == null) {
            return null;
        }
        byte[] encrypted = Base64.getDecoder().decode(ciphertext);
        return new String(decryptBytes(encrypted, iv), StandardCharsets.UTF_8);
    }

    @Override
    public EncryptedBytes encryptBytes(byte[] plaintext) {
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));
            return new EncryptedBytes(cipher.doFinal(plaintext), Base64.getEncoder().encodeToString(iv));
        } catch (Exception exception) {
            throw VaultException.badRequest("Encryption failed");
        }
    }

    @Override
    public byte[] decryptBytes(byte[] ciphertext, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, Base64.getDecoder().decode(iv)));
            return cipher.doFinal(ciphertext);
        } catch (Exception exception) {
            throw VaultException.badRequest("Decryption failed");
        }
    }
}
