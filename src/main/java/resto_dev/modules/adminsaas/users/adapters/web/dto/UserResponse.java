package resto_dev.modules.adminsaas.users.adapters.web.dto;

import java.util.UUID;

/**
 * Response DTO for user data (never exposes password).
 */
public record UserResponse(
                UUID id,
                String email,
                String fullName,
                boolean superAdmin,
                boolean active) {
}
