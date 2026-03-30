package resto_dev.shared.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for AES encryption/decryption.
 */
public class EncryptionUtils {

    private static final String ALGORITHM = "AES";

    public static String encrypt(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(prepareKey(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public static String decrypt(String encryptedData, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(prepareKey(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decodedData), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    private static byte[] prepareKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        // Ensure key is 16 bytes for AES-128 or 32 bytes for AES-256
        byte[] finalKey = new byte[16];
        System.arraycopy(keyBytes, 0, finalKey, 0, Math.min(keyBytes.length, 16));
        return finalKey;
    }
}
