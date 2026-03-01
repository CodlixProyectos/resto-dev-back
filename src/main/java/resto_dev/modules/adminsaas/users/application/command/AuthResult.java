package resto_dev.modules.adminsaas.users.application.command;

import java.util.UUID;

/**
 * Result returned after successful authentication.
 */
public record AuthResult(
                String token,
                UUID userId,
                String email,
                String fullName,
                boolean superAdmin) {
}
