package resto_dev.shared.errors;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when business rules are violated.
 * Results in HTTP 400 BAD REQUEST response.
 */
public class BusinessValidationException extends ApiException {

    public BusinessValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessValidationException invalidReference(String resource, Object identifier) {
        return new BusinessValidationException(
                String.format("%s referenciado no existe: %s", resource, identifier));
    }
}
