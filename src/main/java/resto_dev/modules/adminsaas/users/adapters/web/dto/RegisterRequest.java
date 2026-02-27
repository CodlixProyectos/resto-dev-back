package resto_dev.modules.adminsaas.users.adapters.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for user registration.
 */
public record RegisterRequest(

                @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

                @NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password,

                @NotBlank(message = "Full name is required") @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters") String fullName) {
}
