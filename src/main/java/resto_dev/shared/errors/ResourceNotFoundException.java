package resto_dev.shared.errors;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource (zone, table, etc) is not found.
 * Results in HTTP 404 response.
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super(String.format("%s no encontrado con identificador: %s", resource, identifier), HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
