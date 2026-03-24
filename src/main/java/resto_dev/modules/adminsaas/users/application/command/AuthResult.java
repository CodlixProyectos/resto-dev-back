package resto_dev.modules.adminsaas.users.application.command;

import java.util.UUID;

/**
 * Result returned after successful authentication.
 */
public record AuthResult(
                String token,
                UUID userId,
                UUID memberId,
                UUID organizationId,
                String email,
                String fullName,
                boolean superAdmin,
                String role,
                boolean notificationsEnabled,
                boolean soundEnabled,
                boolean darkModeEnabled) {
}
