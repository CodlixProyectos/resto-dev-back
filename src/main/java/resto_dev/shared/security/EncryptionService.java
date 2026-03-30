package resto_dev.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import resto_dev.shared.utils.EncryptionUtils;

/**
 * Service to handle data encryption using application properties.
 */
@Service
public class EncryptionService {

    @Value("${app.security.encryption-key}")
    private String encryptionKey;

    public String encrypt(String data) {
        if (data == null) return null;
        return EncryptionUtils.encrypt(data, encryptionKey);
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        return EncryptionUtils.decrypt(encryptedData, encryptionKey);
    }
}
