package resto_dev.shared.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA Attribute Converter to automatically encrypt/decrypt database columns.
 */
@Converter
@Component
public class AttributeEncryptionConverter implements AttributeConverter<String, String> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService service) {
        AttributeEncryptionConverter.encryptionService = service;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) return attribute;
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return dbData;
        try {
            return encryptionService.decrypt(dbData);
        } catch (Exception e) {
            // If decryption fails, it might be because it's already plain text (migration period)
            // or wrong key. For now, return as is or handle accordingly.
            return dbData;
        }
    }
}
