package com.alextam.uploadimage;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class CryptoUtil {

    private static final String ALGORITHM = "AES";
    // IMPORTANT: In a real production app, never hardcode the key.
    // This key should be securely stored and managed.
    private static final String SECRET_KEY = "ThisIsASecretKey1234567890123456"; // Must be 16, 24, or 32 bytes long

    /**
     * Encrypts a byte array using AES.
     * @param dataToEncrypt The byte array of the original file.
     * @return The encrypted byte array.
     * @throws Exception if encryption fails.
     */
    public static byte[] encrypt(byte[] dataToEncrypt) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(dataToEncrypt);
    }
    
    // Decryption method is not needed for this flow but included for completeness.
    /*
    public static byte[] decrypt(byte[] encryptedData) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }
    */
}
