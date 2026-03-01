package resto_dev.modules.cashregister.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ShiftAlreadyOpenException extends RuntimeException {
    public ShiftAlreadyOpenException(String message) {
        super(message);
    }
}
