package resto_dev.shared.errors;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to create a resource that already exists.
 * Results in HTTP 409 CONFLICT response.
 */
public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("Ya existe un %s con el %s '%s'", resource, field, value), HttpStatus.CONFLICT);
    }

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
