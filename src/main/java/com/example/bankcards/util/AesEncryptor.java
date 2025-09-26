package com.example.bankcards.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // 96 bits
    private static final int TAG_LENGTH_BIT = 128;

    private final String secret;
    private SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesEncryptor(@Value("${app.aes.key:0123456789abcdef}") String secret) {
        this.secret = secret;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // ensure length 16/24/32
        if (keyBytes.length < 16) {
            byte[] tmp = new byte[16];
            System.arraycopy(keyBytes, 0, tmp, 0, Math.min(keyBytes.length, 16));
            keyBytes = tmp;
        } else if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            // normalize to 32
            byte[] tmp = new byte[32];
            System.arraycopy(keyBytes, 0, tmp, 0, Math.min(keyBytes.length, 32));
            keyBytes = tmp;
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
            bb.put(iv);
            bb.put(cipherText);
            return Base64.getEncoder().encodeToString(bb.array());
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String encoded) {
        try {
            byte[] all = Base64.getDecoder().decode(encoded);
            ByteBuffer bb = ByteBuffer.wrap(all);
            byte[] iv = new byte[IV_LENGTH];
            bb.get(iv);
            byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}
