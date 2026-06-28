package com.enterprise.architecture.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * Core cryptographic engine handling symmetric data protection.
 * Utilizes AES-256 in Galois/Counter Mode (GCM) to enforce confidentiality and
 * data integrity.
 */
public class EncryptionUtil {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    /**
     * Encrypts a raw byte array payload and prepends a cryptographically secure
     * random IV block.
     * 
     * @param plainData Unencrypted input data bytes.
     * @param keyBytes  Symmetric 32-byte secret key Spec.
     * @return Formatted combined byte array block [12-byte IV][Ciphertext bytes].
     */
    public static byte[] encrypt(byte[] plainData, byte[] keyBytes) throws Exception {
        byte[] iv = new byte[IV_LENGTH_BYTE];
        SecureRandom.getInstanceStrong().nextBytes(iv); // Generates unrepeatable cryptographic nonce

        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] encryptedData = cipher.doFinal(plainData);
        byte[] absoluteBlob = new byte[iv.length + encryptedData.length];

        // Structure target: [ 12-byte IV ] [ Ciphertext payload ]
        System.arraycopy(iv, 0, absoluteBlob, 0, iv.length);
        System.arraycopy(encryptedData, 0, absoluteBlob, iv.length, encryptedData.length);
        return absoluteBlob;
    }

    /**
     * Decrypts an unreadable payload blob by stripping and executing the prepended
     * IV parameters.
     * 
     * @param absoluteBlob Combined encryption array block from storage.
     * @param keyBytes     Symmetric 32-byte secret key Spec.
     * @return Original decrypted readable plain byte array.
     */
    public static byte[] decrypt(byte[] absoluteBlob, byte[] keyBytes) throws Exception {
        if (absoluteBlob.length <= IV_LENGTH_BYTE) {
            throw new IllegalArgumentException("Malformed cryptographic blob detected. Array bounds invalid.");
        }

        byte[] iv = new byte[IV_LENGTH_BYTE];
        System.arraycopy(absoluteBlob, 0, iv, 0, iv.length);

        int encryptedLength = absoluteBlob.length - IV_LENGTH_BYTE;
        byte[] encryptedData = new byte[encryptedLength];
        System.arraycopy(absoluteBlob, iv.length, encryptedData, 0, encryptedLength);

        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        return cipher.doFinal(encryptedData);
    }
}
