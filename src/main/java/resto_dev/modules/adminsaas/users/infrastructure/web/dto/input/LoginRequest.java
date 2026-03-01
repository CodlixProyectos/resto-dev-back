package resto_dev.modules.adminsaas.users.infrastructure.web.dto.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for user login.
 */
public record LoginRequest(

        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

        @NotBlank(message = "Password is required") String password) {
}
